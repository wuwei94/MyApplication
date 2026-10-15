package com.example.william.my.module.ml.download

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 * 应用私有目录下的模型缓存。
 *
 * 路径计算一律接收 [File]（`filesDir`）而不是 [Context]，这样这套判定逻辑
 * 不依赖 Android 框架、可以落在纯 JVM 单测覆盖范围内；页面侧用带 [Context] 的重载即可。
 * 公共 API 只做**存在性**判定，不校验哈希：完整文件的落地由 [ModelDownloader] 用
 * `.part` + rename 保证，因此「目标文件存在」即等价于「下载已完成」。
 */
object ModelStore {

    private const val DIR_NAME = "models"

    /** 模型目录（不存在则创建） */
    fun dirIn(filesDir: File): File = File(filesDir, DIR_NAME).apply { mkdirs() }

    /** 某个远端模型落盘后的目标文件 */
    fun fileIn(filesDir: File, model: RemoteModel): File = File(dirIn(filesDir), model.fileName)

    /** 下载中的临时文件；命名固定，中断后仍可被下次续传复用 */
    fun partFileIn(filesDir: File, model: RemoteModel): File = File(dirIn(filesDir), "${model.fileName}.part")

    /** 是否已经下载完成 */
    fun isReadyIn(filesDir: File, model: RemoteModel): Boolean {
        val file = fileIn(filesDir, model)
        return file.isFile && file.length() > 0L
    }

    /** 尚未就绪的模型合计还要下载多少字节（用于「首次使用需下载约 xx MB」提示） */
    fun pendingBytesIn(filesDir: File, models: List<RemoteModel>): Long = models.fold(0L) { sum, model -> sum + if (isReadyIn(filesDir, model)) 0L else model.approxBytes }

    /** 删除模型本体与残留的临时文件 */
    fun deleteIn(filesDir: File, model: RemoteModel) {
        fileIn(filesDir, model).delete()
        partFileIn(filesDir, model).delete()
    }

    fun dir(context: Context): File = dirIn(context.filesDir)

    fun fileFor(context: Context, model: RemoteModel): File = fileIn(context.filesDir, model)

    fun isReady(context: Context, model: RemoteModel): Boolean = isReadyIn(context.filesDir, model)

    fun pendingBytes(context: Context, models: List<RemoteModel>): Long = pendingBytesIn(context.filesDir, models)

    fun delete(context: Context, model: RemoteModel) = deleteIn(context.filesDir, model)

    /** 下载完成的模型绝对路径列表，顺序与入参一致；缺任何一个都会抛异常 */
    fun requirePaths(context: Context, models: List<RemoteModel>): List<String> = models.map { model ->
        val file = fileFor(context, model)
        check(file.isFile && file.length() > 0L) { "模型尚未下载：${model.fileName}" }
        file.absolutePath
    }

    /**
     * 按文件名取绝对路径
     *
     * 落盘文件名与远端文件名一致，因此引擎侧可以直接用文件名拼路径，
     * 不必反过来持有 [RemoteModel] 实例。
     */
    fun pathIn(filesDir: File, fileName: String): String = File(dirIn(filesDir), fileName).absolutePath
}

/**
 * 支持断点续传的模型下载器。
 *
 * 面向的是数十 MB 到数 GB 的模型文件，因此有三点取舍：
 * 1. **断点续传**：先写 `<name>.part`，用 `Range` 请求续下。服务端不支持时（返回 200）
 *    自动退回整文件重下，不会写出错位数据。
 * 2. **原子落地**：只有完整写完后才 rename 成正式文件名，[ModelStore.isReady] 因此
 *    只需判存在性，不会把半截文件误当成可用模型。
 * 3. **可取消**：页面销毁时调 [cancel]，循环内检查标记并抛 [CancellationException]，
 *    `.part` 保留供下次续传。
 *
 * [onProgress] 在后台线程回调，调用方若要更新界面需自行切回主线程。
 */
class ModelDownloader(private val context: Context) {

    @Volatile
    private var cancelled = false

    /** 取消当前下载；已写的 `.part` 会被保留 */
    fun cancel() {
        cancelled = true
    }

    /**
     * 下载单个模型；已存在则直接返回。
     *
     * @throws IOException 网络或服务端异常
     * @throws CancellationException 期间调用了 [cancel]
     */
    suspend fun download(
        model: RemoteModel,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> },
    ): File = withContext(Dispatchers.IO) {
        val target = ModelStore.fileFor(context, model)
        if (target.isFile && target.length() > 0L) {
            onProgress(target.length(), target.length())
            return@withContext target
        }

        val part = ModelStore.partFileIn(context.filesDir, model)
        var downloaded = if (part.isFile) part.length() else 0L

        val connection = (URL(model.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            if (downloaded > 0L) setRequestProperty("Range", "bytes=$downloaded-")
        }

        try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> downloaded = 0L
                HttpURLConnection.HTTP_PARTIAL -> Unit
                // Range 不被满足：.part 长度与服务端不符（内容变了或上次写坏）。
                // 删掉残留，让下一次 ensureAll 重试时从头下，避免永久卡在 416。
                // HttpURLConnection 没有为 416 提供命名常量（只有 206 的 HTTP_PARTIAL），故用字面量。
                416 -> {
                    part.delete()
                    throw IOException("HTTP 416（残留 .part 与远端不符，已清理，请重试）")
                }
                else -> throw IOException("HTTP ${connection.responseCode}")
            }
            val declared = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
            val total = if (declared > 0L) declared + downloaded else model.approxBytes

            connection.inputStream.use { input ->
                RandomAccessFile(part, "rw").use { output ->
                    output.seek(downloaded)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var lastReport = 0L
                    while (true) {
                        if (cancelled) throw CancellationException("模型下载已取消")
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastReport >= PROGRESS_INTERVAL_MS) {
                            lastReport = now
                            onProgress(downloaded, total)
                        }
                    }
                    // 服务端忽略 Range 返回 200 时从文件头重写，此时 downloaded 归零；
                    // 若新内容比原有 .part 短，不截断就会把尾部旧字节带进正式文件
                    output.setLength(downloaded)
                }
            }

            // 长度校验：服务端声明了 Content-Length 时，实下字节数必须一致，否则是被
            // 截断的残次品，不能 rename 落地（否则 ModelStore 会把它当成可用模型）。
            if (declared > 0L && downloaded != declared) {
                part.delete()
                throw IOException("下载被截断：期望 $declared 字节，实得 $downloaded")
            }

            onProgress(downloaded, total)
        } finally {
            connection.disconnect()
        }

        if (target.exists()) target.delete()
        if (!part.renameTo(target)) {
            part.copyTo(target, overwrite = true)
            part.delete()
        }
        target
    }

    /**
     * 按顺序补齐一组模型，任一失败即抛出（已完成的不会重下）。
     *
     * @param onProgress `(当前第几个, 该文件已下字节, 该文件总字节, 全部已下字节, 全部总字节)`
     */
    suspend fun ensureAll(
        models: List<RemoteModel>,
        onProgress: (index: Int, downloaded: Long, total: Long, overall: Long, overallTotal: Long) -> Unit =
            { _, _, _, _, _ -> },
    ): List<File> {
        val sizes = models.map { if (ModelStore.isReady(context, it)) 0L else it.approxBytes }
        val overallTotal = sizes.sum()
        var doneBefore = 0L
        return models.mapIndexed { index, model ->
            val file = download(model) { downloaded, _ ->
                onProgress(index, downloaded, sizes[index], doneBefore + downloaded, overallTotal)
            }
            doneBefore += sizes[index]
            file
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 30_000
        const val BUFFER_SIZE = 64 * 1024
        const val PROGRESS_INTERVAL_MS = 120L
    }
}
