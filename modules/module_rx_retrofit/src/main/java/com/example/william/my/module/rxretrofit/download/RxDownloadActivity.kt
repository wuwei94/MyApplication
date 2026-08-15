package com.example.william.my.module.rxretrofit.download

import android.text.format.Formatter
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.rx.download.RxDownload
import com.example.william.my.core.rx.download.RxDownloadManager
import com.example.william.my.core.rx.download.callback.RxDownloadCallback
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult
import com.example.william.my.core.rx.download.queue.model.DownloadQueueProgress
import com.example.william.my.core.rx.download.queue.model.DownloadQueueResult
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.io.File

/** 单文件与批量并发下载示例。 */
@Route(path = RouterPath.RxRetrofit.Download)
class RxDownloadActivity : BasicResponseActivity() {

    private val downloadRetrofit = rxRetrofit {
        client(OkHttpClient.Builder().retryOnConnectionFailure(false).build())
    }
    private val operations = CompositeDisposable()
    private var downloadOperationId = 0L
    private var downloadInProgress = false
    private var downloadCleanupRequested = false

    /** 下载示例文件保存目录。 */
    private val downloadDirectory = File(cacheDir, "downloads")

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "单文件下载",
            "多文件下载",
            "取消当前下载",
            "清理下载文件",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> downloadSingleFile()
            1 -> downloadMultipleFiles()
            2 -> cancelDownload()
            3 -> clearDownloads()
        }
    }

    private fun downloadSingleFile() {
        val operationId = ++downloadOperationId
        operations.clear()
        clearUpdatingLogs()
        downloadInProgress = true
        downloadCleanupRequested = false
        appendLog("单文件下载已开始")

        val destination = File(downloadDirectory, "single-${Constants.Url_Download.fileName()}")
        operations.add(
            RxDownload.builder()
                .api(Constants.Url_Download)
                .retrofit(downloadRetrofit)
                .destination(destination)
                .setProvider(this)
                .onFinally {
                    if (downloadOperationId != operationId) return@onFinally
                    downloadInProgress = false
                    if (downloadCleanupRequested) {
                        downloadCleanupRequested = false
                        runOnUiThread {
                            if (downloadOperationId == operationId) clearDownloadFiles()
                        }
                    }
                }
                .build()
                .subscribeWith(
                    object : RxDownloadCallback<DownloadProgress, DownloadResult>() {
                        override fun onProgress(progress: DownloadProgress) {
                            updateDownloadProgress(progress)
                        }

                        override fun onResponse(response: DownloadResult) {
                            removeUpdatingLog(DOWNLOAD_PROGRESS_KEY)
                            appendLog("文件已保存到：${response.file.absolutePath}")
                        }

                        override fun onFailure(error: ApiException) {
                            removeUpdatingLog(DOWNLOAD_PROGRESS_KEY)
                            appendLog("下载失败：${error.message}")
                        }
                    }
                )
        )
    }

    private fun downloadMultipleFiles() {
        val operationId = ++downloadOperationId
        operations.clear()
        clearUpdatingLogs()
        downloadInProgress = true
        downloadCleanupRequested = false
        val tasks = DOWNLOAD_URLS.mapIndexed { index, url ->
            val destination = File(downloadDirectory, "${index + 1}-${url.fileName()}")
            DownloadQueueTask(url = url, destination = destination, id = destination.name)
        }
        appendLog("多文件下载已开始，共 ${tasks.size} 个文件")

        operations.add(
            RxDownloadManager.builder()
                .retrofit(downloadRetrofit)
                .maxConcurrency(QUEUE_MAX_CONCURRENCY)
                .build()
                .download()
                .addTasks(tasks)
                .setProvider(this)
                .onFinally {
                    if (downloadOperationId != operationId) return@onFinally
                    downloadInProgress = false
                    if (downloadCleanupRequested) {
                        downloadCleanupRequested = false
                        runOnUiThread {
                            if (downloadOperationId == operationId) clearDownloadFiles()
                        }
                    }
                }
                .build()
                .subscribeWith(
                    object : RxDownloadCallback<DownloadQueueProgress, DownloadQueueResult>() {
                        override fun onProgress(progress: DownloadQueueProgress) {
                            updateQueueProgress(progress)
                        }

                        override fun onResponse(response: DownloadQueueResult) {
                            removeUpdatingLog(DOWNLOAD_PROGRESS_KEY)
                            response.successes.forEach { success ->
                                appendLog(
                                    "${success.task.id} 已保存到：" +
                                        success.result.file.absolutePath
                                )
                            }
                            response.failures.forEach { failure ->
                                appendLog(
                                    "${failure.task.id} 下载失败：" +
                                        failure.error.message
                                )
                            }
                            appendLog(
                                "下载完成：成功 ${response.successes.size}，" +
                                    "失败 ${response.failures.size}"
                            )
                        }

                        override fun onFailure(error: ApiException) {
                            removeUpdatingLog(DOWNLOAD_PROGRESS_KEY)
                            appendLog("下载队列失败：${error.message}")
                        }
                    }
                )
        )
    }

    private fun cancelDownload() {
        operations.clear()
        clearUpdatingLogs()
        appendLog("当前下载操作已取消")
    }

    private fun clearDownloads() {
        clearUpdatingLogs()
        if (downloadInProgress) {
            downloadCleanupRequested = true
            operations.clear()
            appendLog("当前下载已取消，等待任务结束后清理文件")
            return
        }
        clearDownloadFiles()
    }

    /** 删除下载示例目录中的直接子项。 */
    private fun clearDownloadFiles() {
        if (downloadDirectory.exists() && !downloadDirectory.isDirectory) {
            appendLog("下载文件清理失败：目标路径不是目录")
            return
        }
        if (!downloadDirectory.exists()) {
            appendLog("下载文件清理完成：已删除 0/0")
            return
        }
        val files = downloadDirectory.listFiles() ?: run {
            appendLog("下载文件清理失败：无法读取目录")
            return
        }
        val failedFiles = files.filter { !it.delete() }
        appendLog(
            "下载文件清理完成：已删除 " +
                "${files.size - failedFiles.size}/${files.size}"
        )
        if (failedFiles.isNotEmpty()) {
            appendLog("未能删除：${failedFiles.joinToString { it.name }}")
        }
    }

    /** 更新单文件下载进度日志。 */
    private fun updateDownloadProgress(progress: DownloadProgress) {
        val percent = progress.percent
        val message = if (percent == null) {
            "下载进度：已下载 ${formatBytes(progress.currentBytes)}（总大小未知）"
        } else {
            "下载进度：${formatBytes(progress.currentBytes)} / " +
                "${formatBytes(progress.totalBytes)}（$percent%）"
        }
        updateLog(DOWNLOAD_PROGRESS_KEY, message)
    }

    /** 更新批量下载队列进度日志。 */
    private fun updateQueueProgress(progress: DownloadQueueProgress) {
        val percent = progress.percent
        val message = if (percent == null) {
            "下载进度：完成 ${progress.completedCount}/${progress.totalCount}，" +
                "已下载 ${formatBytes(progress.currentBytes)}（总大小未知）"
        } else {
            "下载进度：完成 ${progress.completedCount}/${progress.totalCount}，" +
                "${formatBytes(progress.currentBytes)} / ${formatBytes(progress.totalBytes)}" +
                "（$percent%）"
        }
        updateLog(DOWNLOAD_PROGRESS_KEY, message)
    }

    /** 将字节数格式化为适合日志显示的文本。 */
    private fun formatBytes(bytes: Long): String {
        return Formatter.formatFileSize(this, bytes.coerceAtLeast(0L))
    }
    override fun onDestroy() {
        downloadOperationId++
        operations.dispose()
        super.onDestroy()
    }

    /** 从下载地址中提取并清理本地文件名。 */
    private fun String.fileName(): String {
        val rawName = toHttpUrlOrNull()
            ?.pathSegments
            ?.lastOrNull()
            ?.takeIf(String::isNotBlank)
            ?: "download.bin"
        return rawName.replace(FILE_NAME_INVALID_CHARS, "_").take(MAX_FILE_NAME_LENGTH)
    }


    private companion object {
        const val MAX_FILE_NAME_LENGTH = 96
        const val QUEUE_MAX_CONCURRENCY = 3
        const val DOWNLOAD_PROGRESS_KEY = "download-progress"
        val FILE_NAME_INVALID_CHARS = Regex("[^A-Za-z0-9._-]")
        val DOWNLOAD_URLS = listOf(
            Constants.Url_Download,
            Constants.Url_Ludo,
            Constants.Url_BombCat,
        )
    }
}
