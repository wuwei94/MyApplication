package com.example.william.my.module.http.rx_request.activity

import android.os.Bundle
import android.text.format.Formatter
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.rx.upload.RxUpload
import com.example.william.my.core.rx.upload.callback.RxUploadCallback
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.example.william.my.core.rx.upload.model.UploadResult
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import java.io.File

/**
 * RxUpload — Retrofit + RxJava 文件上传封装
 *
 * RxUpload 基于 Retrofit Multipart 封装单文件与多文件上传，
 * 提供进度回调、生命周期绑定与批量并发队列能力。
 *
 * 核心机制与避坑点：
 * 1. Multipart 上传：支持单文件与多文件字段映射
 * 2. 进度回调：RxUploadCallback.onProgress 原位回报上传进度
 * 3. 生命周期绑定：setProvider(owner) 与 onFinally 统一收尾
 * 4. 批量并发：多文件任务共享同一 Retrofit 实例并发上传
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RxUpload)
class RxUploadActivity : BasicResponseActivity() {

    private val uploadRetrofit = rxRetrofit {
        client(OkHttpClient.Builder().retryOnConnectionFailure(false).build())
    }
    private val operations = CompositeDisposable()
    private var uploadOperationId = 0L
    private var uploadInProgress = false
    private var uploadCleanupRequested = false

    /** 上传示例文件保存目录。 */
    private val uploadDirectory = File(cacheDir, "uploads")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("单文件与批量并发上传示例，进度在下方日志区原位更新")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 单文件上传",
        "2. 多文件上传",
        "3. 取消当前上传",
        "清理上传临时文件",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> uploadSingleFile()
            1 -> uploadMultipleFiles()
            2 -> cancelUpload()
            3 -> clearUploads()
        }
    }

    private fun uploadSingleFile() {
        val operationId = ++uploadOperationId
        operations.clear()
        clearUpdatingLogs()
        uploadInProgress = false
        uploadCleanupRequested = false
        val file = File(uploadDirectory, "module-http-sample-1.txt")
        val contents = mapOf(
            file to "module_http 上传示例文件 1",
        )
        try {
            contents.forEach { (target, content) ->
                target.parentFile?.mkdirs()
                target.writeText(content)
            }
        } catch (error: Exception) {
            appendLog("✗ [单文件上传] 文件创建失败：${error.message ?: ApiException.DEFAULT_MESSAGE}")
            return
        }
        appendLog("→ [单文件上传] 已开始")
        uploadInProgress = true
        operations.add(
            RxUpload.builder()
                .api(Constants.Url_Upload)
                .retrofit(uploadRetrofit)
                .addParam("source", "module_http")
                .addFile("files", file)
                .setProvider(this)
                .onFinally {
                    if (uploadOperationId != operationId) return@onFinally
                    uploadInProgress = false
                    if (uploadCleanupRequested) {
                        uploadCleanupRequested = false
                        runOnUiThread {
                            if (uploadOperationId == operationId) clearUploadFiles()
                        }
                    }
                }
                .build()
                .subscribeWith(object : RxUploadCallback() {
                    override fun onProgress(progress: UploadProgress) {
                        updateUploadProgress(progress)
                    }

                    override fun onResponse(response: UploadResult) {
                        removeUpdatingLog(UPLOAD_PROGRESS_KEY)
                        appendLog("✓ [单文件上传] 完成，HTTP ${response.statusCode}")
                        appendFormatLog("✓ [单文件上传] 响应：", response.body)
                    }

                    override fun onFailure(error: ApiException) {
                        removeUpdatingLog(UPLOAD_PROGRESS_KEY)
                        appendLog("✗ [单文件上传] 失败：${error.message}")
                    }
                }),
        )
    }

    private fun uploadMultipleFiles() {
        val operationId = ++uploadOperationId
        operations.clear()
        clearUpdatingLogs()
        uploadInProgress = false
        uploadCleanupRequested = false
        val files = (1..2).map { index ->
            File(uploadDirectory, "module-http-sample-$index.txt")
        }
        val contents = files.mapIndexed { index, file ->
            file to "module_http 上传示例文件 ${index + 1}"
        }.toMap()
        try {
            contents.forEach { (target, content) ->
                target.parentFile?.mkdirs()
                target.writeText(content)
            }
        } catch (error: Exception) {
            appendLog("✗ [多文件上传] 文件创建失败：${error.message ?: ApiException.DEFAULT_MESSAGE}")
            return
        }
        appendLog("→ [多文件上传] 已开始，共 ${files.size} 个文件")
        uploadInProgress = true
        operations.add(
            RxUpload.builder()
                .api(Constants.Url_Upload)
                .retrofit(uploadRetrofit)
                .addParam("source", "module_http")
                .addFiles("files", files)
                .setProvider(this)
                .onFinally {
                    if (uploadOperationId != operationId) return@onFinally
                    uploadInProgress = false
                    if (uploadCleanupRequested) {
                        uploadCleanupRequested = false
                        runOnUiThread {
                            if (uploadOperationId == operationId) clearUploadFiles()
                        }
                    }
                }
                .build()
                .subscribeWith(object : RxUploadCallback() {
                    override fun onProgress(progress: UploadProgress) {
                        updateUploadProgress(progress)
                    }

                    override fun onResponse(response: UploadResult) {
                        removeUpdatingLog(UPLOAD_PROGRESS_KEY)
                        appendLog("✓ [多文件上传] 完成，HTTP ${response.statusCode}")
                        appendFormatLog("✓ [多文件上传] 响应：", response.body)
                    }

                    override fun onFailure(error: ApiException) {
                        removeUpdatingLog(UPLOAD_PROGRESS_KEY)
                        appendLog("✗ [多文件上传] 失败：${error.message}")
                    }
                }),
        )
    }

    private fun cancelUpload() {
        operations.clear()
        clearUpdatingLogs()
        appendLog("→ [上传] 当前操作已取消")
    }

    private fun clearUploads() {
        clearUpdatingLogs()
        if (uploadInProgress) {
            uploadCleanupRequested = true
            operations.clear()
            appendLog("→ [上传] 当前任务已取消，等待结束后清理文件")
            return
        }
        clearUploadFiles()
    }

    /** 删除上传示例目录中的直接子项。 */
    private fun clearUploadFiles() {
        if (uploadDirectory.exists() && !uploadDirectory.isDirectory) {
            appendLog("✗ [清理上传文件] 目标路径不是目录")
            return
        }
        if (!uploadDirectory.exists()) {
            appendLog("✓ [清理上传文件] 已删除 0/0")
            return
        }
        val files = uploadDirectory.listFiles() ?: run {
            appendLog("✗ [清理上传文件] 无法读取目录")
            return
        }
        val failedFiles = files.filter { !it.delete() }
        appendLog(
            "✓ [清理上传文件] 已删除 " +
                "${files.size - failedFiles.size}/${files.size}",
        )
        if (failedFiles.isNotEmpty()) {
            appendLog("✗ [清理上传文件] 未能删除：${failedFiles.joinToString { it.name }}")
        }
    }

    /** 更新上传进度日志。 */
    private fun updateUploadProgress(progress: UploadProgress) {
        val percent = progress.percent ?: return
        updateLog(
            UPLOAD_PROGRESS_KEY,
            "上传进度：${formatBytes(progress.currentBytes)} / " +
                "${formatBytes(progress.totalBytes)}（$percent%）",
        )
    }

    /** 将字节数格式化为适合日志显示的文本。 */
    private fun formatBytes(bytes: Long): String = Formatter.formatFileSize(this, bytes.coerceAtLeast(0L))

    override fun onDestroy() {
        uploadOperationId++
        operations.dispose()
        super.onDestroy()
    }

    private companion object {
        const val UPLOAD_PROGRESS_KEY = "upload-progress"
    }
}
