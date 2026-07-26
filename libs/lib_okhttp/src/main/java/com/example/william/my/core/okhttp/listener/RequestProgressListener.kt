package com.example.william.my.core.okhttp.listener

/**
 * 上传进度接口
 */
@Deprecated(
    message = "请使用 InterceptorUploadProgress 配合 lambda 替代",
    replaceWith = ReplaceWith("InterceptorUploadProgress")
)
interface RequestProgressListener {
    /**
     * 上传进度
     *
     * @param currentSize
     * @param totalSize
     */
    fun onProgress(currentSize: Long, totalSize: Long)
}