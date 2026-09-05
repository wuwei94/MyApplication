package com.example.william.my.core.okhttp.body

import com.example.william.my.core.okhttp.listener.RequestProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

/**
 * 旧版上传进度请求体
 *
 * 新代码请使用 [UploadProgressRequestBody]。
 */
@Deprecated(
    message = "请使用 UploadProgressRequestBody",
    replaceWith = ReplaceWith("UploadProgressRequestBody(requestBody, listener)"),
)
class RequestBodyProgress(
    mRequestBody: RequestBody,
    mRequestProgressListener: RequestProgressListener?,
) : RequestBody() {

    private val delegate = UploadProgressRequestBody(mRequestBody) { currentBytes, totalBytes ->
        mRequestProgressListener?.onProgress(currentBytes, totalBytes)
    }

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun isOneShot(): Boolean = delegate.isOneShot()

    override fun isDuplex(): Boolean = delegate.isDuplex()

    override fun writeTo(sink: BufferedSink) = delegate.writeTo(sink)
}
