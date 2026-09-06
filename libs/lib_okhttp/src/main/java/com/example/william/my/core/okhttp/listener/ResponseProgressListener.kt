package com.example.william.my.core.okhttp.listener

/**
 * 下载进度监听接口
 */
@Deprecated(
    message = "请使用 InterceptorDownloadProgress 配合 lambda 替代",
    replaceWith = ReplaceWith("InterceptorDownloadProgress"),
)
interface ResponseProgressListener {
    /**
     * 下载进度
     */
    fun onProgress(url: String, currentSize: Long, totalSize: Long)
}
