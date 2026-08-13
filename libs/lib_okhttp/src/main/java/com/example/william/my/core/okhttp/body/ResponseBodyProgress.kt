package com.example.william.my.core.okhttp.body

import com.example.william.my.core.okhttp.listener.ResponseProgressListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource

/**
 * 旧版下载进度响应体
 *
 * 新代码请使用 [DownloadProgressResponseBody]。
 */
@Deprecated(
    message = "请使用 DownloadProgressResponseBody",
    replaceWith = ReplaceWith("DownloadProgressResponseBody(url, responseBody, listener)")
)
class ResponseBodyProgress(
    mUrl: String,
    mResponseBody: ResponseBody,
    mResponseProgressListener: ResponseProgressListener
) : ResponseBody() {

    private val delegate = DownloadProgressResponseBody(
        mUrl,
        mResponseBody,
        mResponseProgressListener::onProgress
    )

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun source(): BufferedSource = delegate.source()
}
