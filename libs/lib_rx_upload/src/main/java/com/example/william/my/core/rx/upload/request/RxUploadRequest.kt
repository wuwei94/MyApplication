package com.example.william.my.core.rx.upload.request

import com.example.william.my.core.okhttp.body.UploadProgressRequestBody
import com.example.william.my.core.rx.upload.callback.RxUploadCallback
import com.example.william.my.core.rx.upload.config.UploadConfig
import com.example.william.my.core.rx.upload.exception.UploadHttpException
import com.example.william.my.core.rx.upload.exception.toUploadApiException
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.example.william.my.core.rx.upload.model.UploadResult
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import java.io.InterruptedIOException

/**
 * 可重复订阅的不可变 Rx POST 上传请求，每次订阅都会执行一次新的 HTTP 请求
 */
class RxUploadRequest internal constructor(
    private val config: UploadConfig,
) {

    fun asSingle(): Single<UploadResult> {
        return Single.defer { createSingle(callbackProgress = null) }
    }

    /** 使用上传回调订阅请求，并返回用于取消的 Disposable。 */
    fun subscribeWith(callback: RxUploadCallback): Disposable {
        return Single.defer { createSingle(callback::onProgress) }
            .doOnSubscribe { callback.onLoading() }
            .subscribe(
                callback::onResponse,
                { error -> callback.onFailure(error.toUploadApiException()) },
            )
    }

    private fun createSingle(
        callbackProgress: ((UploadProgress) -> Unit)?,
    ): Single<UploadResult> {
        val termination = OperationTermination(config.onFinally)
        var source: Single<UploadResult> = Single.create { emitter ->
            if (emitter.isDisposed) return@create
            val progressDispatcher = ProgressDispatcher(
                scheduler = config.progressScheduler ?: AndroidSchedulers.mainThread(),
                listener = config.onProgress,
                callback = callbackProgress,
            )
            if (!termination.start()) {
                progressDispatcher.dispose()
                emitter.onError(InterruptedIOException("上传任务已取消"))
                return@create
            }
            try {
                val call = createRxApi(UploadApi::class.java, config.retrofit).upload(
                    url = config.url,
                    headers = config.headers,
                    body = buildBody { progress ->
                        if (!emitter.isDisposed) {
                            progressDispatcher.dispatch(progress)
                        }
                    },
                )
                emitter.setCancellable {
                    progressDispatcher.dispose()
                    call.cancel()
                }
                val response = call.execute()
                val responseBody = (response.body() ?: response.errorBody())
                    ?.use { body -> body.string() }
                    .orEmpty()
                if (!response.isSuccessful) {
                    throw UploadHttpException(response.code(), responseBody, response.message())
                }
                if (!emitter.isDisposed) {
                    val result = UploadResult(
                        statusCode = response.code(),
                        message = response.message(),
                        headers = response.headers().toMultimap(),
                        body = responseBody,
                    )
                    progressDispatcher.terminate {
                        if (!emitter.isDisposed) {
                            emitter.onSuccess(result)
                        }
                    }
                }
            } catch (error: Exception) {
                progressDispatcher.terminate {
                    if (!emitter.isDisposed) emitter.onError(error)
                }
            } finally {
                termination.finish()
            }
        }
        source = source.doOnDispose(termination::close)
        config.lifecycle?.let { lifecycle ->
            source = source.compose(lifecycle.bindToLifecycle())
        }
        return source.subscribeOn(config.subscribeScheduler)
            .observeOn(config.observeScheduler ?: AndroidSchedulers.mainThread())
            .doOnDispose(termination::close)
    }

    private fun buildBody(progress: (UploadProgress) -> Unit): RequestBody {
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                config.formFields.forEach { (name, value) -> addFormDataPart(name, value) }
                config.files.forEach { part ->
                    val mediaType = part.mediaType ?: DEFAULT_FILE_MEDIA_TYPE
                    addFormDataPart(
                        part.name,
                        part.fileName,
                        part.file.asRequestBody(mediaType),
                    )
                }
            }
            .build()

        var lastProgressAt = 0L
        val body = UploadProgressRequestBody(multipart) { currentBytes, totalBytes ->
            val now = System.currentTimeMillis()
            if (
                currentBytes == totalBytes ||
                now - lastProgressAt >= config.progressIntervalMillis
            ) {
                progress(UploadProgress(currentBytes, totalBytes))
                lastProgressAt = now
            }
        }
        return body
    }

    internal companion object {
        val defaultRetrofit: Retrofit by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            rxRetrofit {
                client(
                    OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .build()
                )
            }
        }

        fun withoutConnectionFailureRetry(retrofit: Retrofit): Retrofit {
            val client = retrofit.callFactory() as? OkHttpClient
                ?: throw IllegalArgumentException(
                    "上传 Retrofit 必须使用 OkHttpClient 作为 Call.Factory"
                )
            if (!client.retryOnConnectionFailure) return retrofit
            return retrofit.newBuilder()
                .client(client.newBuilder().retryOnConnectionFailure(false).build())
                .build()
        }

        private val DEFAULT_FILE_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }

    private class ProgressDispatcher(
        scheduler: Scheduler,
        private val listener: ((UploadProgress) -> Unit)?,
        private val callback: ((UploadProgress) -> Unit)?,
    ) {
        private val worker = if (listener != null || callback != null) {
            scheduler.createWorker()
        } else {
            null
        }

        fun dispatch(progress: UploadProgress) {
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
        private val onFinally: (() -> Unit)?,
    ) {
        private var started = false
        private var closed = false
        private var notified = false

        @Synchronized
        fun start(): Boolean {
            if (closed) return false
            started = true
            return true
        }

        fun finish() {
            notifyFinally(requireStarted = true)
        }

        fun close() {
            val shouldNotify = synchronized(this) {
                closed = true
                !started
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
