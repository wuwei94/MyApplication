package com.example.william.my.core.rx.download.request

import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.rx.download.callback.RxDownloadCallback
import com.example.william.my.core.rx.download.config.DownloadConfig
import com.example.william.my.core.rx.download.exception.DownloadHttpException
import com.example.william.my.core.rx.download.exception.toDownloadApiException
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult
import com.example.william.my.core.rx.download.resume.DownloadResumeMetadata
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InterruptedIOException

/** 可重复订阅的不可变 Rx 下载请求。每次订阅都会执行一次新的 HTTP 请求。 */
class RxDownloadRequest internal constructor(
    private val config: DownloadConfig,
) {

    fun asSingle(): Single<DownloadResult> = Single.defer { createSingle(callbackProgress = null) }

    /** 使用统一下载回调订阅单任务，并返回用于取消的 Disposable。 */
    fun subscribeWith(
        callback: RxDownloadCallback<DownloadProgress, DownloadResult>,
    ): Disposable = Single.defer { createSingle(callback::onProgress) }
        .doOnSubscribe { callback.onLoading() }
        .subscribe(
            callback::onResponse,
            { error -> callback.onFailure(error.toDownloadApiException()) },
        )

    private fun createSingle(
        callbackProgress: ((DownloadProgress) -> Unit)?,
    ): Single<DownloadResult> {
        val termination = OperationTermination(
            onStart = config.onOperationStart,
            onFinally = config.onFinally,
        )
        var source: Single<DownloadResult> = Single.create { emitter ->
            if (emitter.isDisposed) return@create
            val progressDispatcher = ProgressDispatcher(
                scheduler = config.progressScheduler ?: AndroidSchedulers.mainThread(),
                listener = config.onProgress,
                callback = callbackProgress,
            )
            val operationStarted = termination.start()
            if (!operationStarted) {
                progressDispatcher.dispose()
                emitter.onError(InterruptedIOException("下载任务已取消"))
                return@create
            }
            try {
                val resumeContext = prepareResume()
                val call = createRxApi(DownloadApi::class.java, config.retrofit).download(
                    url = config.url,
                    headers = buildHeaders(resumeContext),
                )
                emitter.setCancellable {
                    progressDispatcher.dispose()
                    call.cancel()
                }
                val response = call.execute()
                try {
                    val result = writeResponse(response, resumeContext) { progress ->
                        if (!emitter.isDisposed) {
                            progressDispatcher.dispatch(progress)
                        }
                    }
                    progressDispatcher.terminate {
                        if (!emitter.isDisposed) emitter.onSuccess(result)
                    }
                } finally {
                    response.body()?.close()
                    response.errorBody()?.close()
                }
            } catch (error: Exception) {
                progressDispatcher.terminate {
                    if (!emitter.isDisposed) emitter.onError(error)
                }
            } finally {
                termination.finish()
            }
        }
        config.lifecycle?.let { lifecycle ->
            source = source.compose(lifecycle.bindToLifecycle())
        }
        return source.subscribeOn(config.subscribeScheduler)
            .observeOn(config.observeScheduler ?: AndroidSchedulers.mainThread())
            .doOnDispose(termination::close)
    }

    private fun buildHeaders(resume: ResumeContext): Map<String, String> = config.headers.toMutableMap().apply {
        if (resume.offset > 0L) {
            this["Range"] = "bytes=${resume.offset}-"
            this["If-Range"] = requireNotNull(resume.ifRange)
        }
    }

    private fun writeResponse(
        response: Response<ResponseBody>,
        resume: ResumeContext,
        progress: (DownloadProgress) -> Unit,
    ): DownloadResult {
        if (
            response.code() == HTTP_RANGE_NOT_SATISFIABLE &&
            completedByRange(response.headers(), resume.offset)
        ) {
            progress(DownloadProgress(resume.offset, resume.offset))
            commitPartialFile(resume.partialFile)
            resume.metadataFile.delete()
            return DownloadResult(config.destination, resume.offset, resumed = true)
        }
        if (!response.isSuccessful) {
            val responseBody = response.errorBody()?.string().orEmpty()
            throw DownloadHttpException(response.code(), responseBody, response.message())
        }

        val append = resume.offset > 0L && response.code() == HTTP_PARTIAL_CONTENT
        val startOffset = if (append) resume.offset else 0L
        if (append) validateContentRange(response.headers(), resume.offset)
        val body = response.body() ?: throw IOException("下载响应体为空")
        val totalBytes = responseTotalBytes(response.headers(), startOffset, body.contentLength())
        ensureParentDirectory()

        var currentBytes = startOffset
        var lastProgressAt = 0L
        progress(DownloadProgress(currentBytes, totalBytes))
        body.byteStream().use { input ->
            FileOutputStream(resume.partialFile, append).buffered(BUFFER_SIZE).use { output ->
                if (!append) {
                    DownloadResumeMetadata.from(response.headers(), config.url)
                        .save(resume.metadataFile)
                }
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    if (Thread.currentThread().isInterrupted) {
                        throw InterruptedIOException("下载已取消")
                    }
                    val count = input.read(buffer)
                    if (count == -1) break
                    output.write(buffer, 0, count)
                    currentBytes += count
                    val now = System.currentTimeMillis()
                    if (now - lastProgressAt >= config.progressIntervalMillis) {
                        progress(DownloadProgress(currentBytes, totalBytes))
                        lastProgressAt = now
                    }
                }
            }
        }
        if (totalBytes >= 0L && currentBytes != totalBytes) {
            throw IOException("下载内容不完整：$currentBytes/$totalBytes 字节")
        }
        progress(DownloadProgress(currentBytes, totalBytes))
        commitPartialFile(resume.partialFile)
        resume.metadataFile.delete()
        return DownloadResult(config.destination, currentBytes, resumed = append)
    }

    private fun prepareResume(): ResumeContext {
        ensureParentDirectory()
        recoverDestinationBackup()
        val partialFile = File(config.destination.path + PARTIAL_SUFFIX)
        val metadataFile = File(config.destination.path + METADATA_SUFFIX)
        if (!config.resume) {
            deleteOrThrow(partialFile, "下载临时文件")
            deleteOrThrow(metadataFile, "下载续传元数据")
        }
        val metadata = if (config.resume) {
            DownloadResumeMetadata.load(metadataFile)
        } else {
            null
        }
        val validator = metadata
            ?.takeIf { value -> value.url == config.url }
            ?.ifRange
        val offset = if (partialFile.isFile && validator != null) {
            partialFile.length()
        } else {
            0L
        }
        return ResumeContext(
            partialFile = partialFile,
            metadataFile = metadataFile,
            offset = offset.takeIf { it > 0L } ?: 0L,
            ifRange = validator?.takeIf { offset > 0L },
        )
    }

    private fun validateContentRange(headers: Headers, requestedOffset: Long) {
        val contentRange = headers["Content-Range"]
            ?: throw IOException("续传响应缺少 Content-Range")
        val match = CONTENT_RANGE.matchEntire(contentRange.trim())
            ?: throw IOException("服务端返回的 Content-Range 无效：$contentRange")
        val start = match.groupValues[1].toLong()
        if (start != requestedOffset) {
            throw IOException("服务端返回的 Content-Range 起点不匹配：$contentRange")
        }
    }

    private fun responseTotalBytes(headers: Headers, offset: Long, contentLength: Long): Long {
        val rangeTotal = headers["Content-Range"]
            ?.let(CONTENT_RANGE::find)
            ?.groupValues
            ?.get(3)
            ?.takeUnless { it == "*" }
            ?.toLongOrNull()
        return rangeTotal ?: if (contentLength >= 0L) offset + contentLength else -1L
    }

    private fun completedByRange(headers: Headers, offset: Long): Boolean {
        val total = headers["Content-Range"]
            ?.let(UNSATISFIED_RANGE::matchEntire)
            ?.groupValues
            ?.get(1)
            ?.toLongOrNull()
        return offset > 0L && total == offset
    }

    private fun ensureParentDirectory() {
        val parent = config.destination.absoluteFile.parentFile ?: return
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("无法创建下载目录：${parent.path}")
        }
        if (!parent.isDirectory) throw IOException("下载目录无效：${parent.path}")
    }

    private fun commitPartialFile(partialFile: File) {
        if (!partialFile.isFile) {
            throw IOException("下载临时文件不存在：${partialFile.path}")
        }
        if (partialFile.renameTo(config.destination)) return

        val backup = destinationBackupFile()
        if (backup.exists() && !backup.delete()) {
            throw IOException("无法清理下载备份文件：${backup.path}")
        }
        val hadDestination = config.destination.exists()
        if (hadDestination && !config.destination.renameTo(backup)) {
            throw IOException("无法备份原下载文件：${config.destination.path}")
        }
        if (partialFile.renameTo(config.destination)) {
            backup.delete()
            return
        }
        if (hadDestination) backup.renameTo(config.destination)
        throw IOException("无法提交下载文件：${config.destination.path}")
    }

    private fun recoverDestinationBackup() {
        val backup = destinationBackupFile()
        when {
            !backup.exists() -> Unit
            config.destination.exists() -> backup.delete()
            !backup.renameTo(config.destination) -> {
                throw IOException("无法恢复下载备份文件：${backup.path}")
            }
        }
    }

    private fun destinationBackupFile(): File = File(config.destination.path + BACKUP_SUFFIX)

    private fun deleteOrThrow(file: File, description: String) {
        if (file.exists() && !file.delete()) {
            throw IOException("无法清理$description：${file.path}")
        }
    }

    internal companion object {
        val defaultRetrofit: Retrofit by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            rxRetrofit()
        }

        private const val BUFFER_SIZE = 64 * 1024
        private const val HTTP_PARTIAL_CONTENT = 206
        private const val HTTP_RANGE_NOT_SATISFIABLE = 416
        internal const val PARTIAL_SUFFIX = ".rxdownload.part"
        internal const val METADATA_SUFFIX = ".rxdownload.properties"
        private const val BACKUP_SUFFIX = ".rxdownload.backup"
        private val CONTENT_RANGE = Regex("bytes\\s+(\\d+)-(\\d+)/(\\d+|\\*)", RegexOption.IGNORE_CASE)
        private val UNSATISFIED_RANGE = Regex("bytes\\s+\\*/(\\d+)", RegexOption.IGNORE_CASE)
    }

    private data class ResumeContext(
        val partialFile: File,
        val metadataFile: File,
        val offset: Long,
        val ifRange: String?,
    )

    private class ProgressDispatcher(
        scheduler: Scheduler,
        private val listener: ((DownloadProgress) -> Unit)?,
        private val callback: ((DownloadProgress) -> Unit)?,
    ) {
        private val worker = if (listener != null || callback != null) {
            scheduler.createWorker()
        } else {
            null
        }

        fun dispatch(progress: DownloadProgress) {
            worker?.schedule {
                listener?.invoke(progress)
                callback?.invoke(progress)
            }
        }

        fun terminate(action: () -> Unit) {
            val progressWorker = worker
            if (progressWorker == null) {
                action()
                return
            }
            progressWorker.schedule {
                try {
                    action()
                } finally {
                    progressWorker.dispose()
                }
            }
        }

        fun dispose() {
            worker?.dispose()
        }
    }

    private class OperationTermination(
        private val onStart: (() -> Boolean)?,
        private val onFinally: (() -> Unit)?,
    ) {
        private var started = false
        private var closed = false
        private var notified = false

        @Synchronized
        fun start(): Boolean {
            if (closed) return false
            if (onStart?.invoke() == false) return false
            started = true
            return true
        }

        fun finish() {
            notifyFinally(requireStarted = true)
        }

        fun close() {
            val shouldNotify = synchronized(this) {
                closed = true
                !started && onStart == null
            }
            if (shouldNotify) notifyFinally(requireStarted = false)
        }

        private fun notifyFinally(requireStarted: Boolean) {
            val action = synchronized(this) {
                if (notified || requireStarted && !started) return
                notified = true
                onFinally
            }
            runCatching { action?.invoke() }
        }
    }
}
