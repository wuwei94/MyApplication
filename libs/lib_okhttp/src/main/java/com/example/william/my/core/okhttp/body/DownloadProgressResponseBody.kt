package com.example.william.my.core.okhttp.body

import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.buffer

/**
 * 下载进度响应体
 *
 * 包装原始 [ResponseBody] 并在读取过程中回调请求地址、已读取字节数和总字节数。
 */
class DownloadProgressResponseBody(
    private val url: String,
    private val delegate: ResponseBody,
    private val listener: (url: String, Long, Long) -> Unit,
) : ResponseBody() {

    private val progressSource =
        ProgressSource(delegate.source(), url, contentLength(), listener).buffer()

    override fun contentType() = delegate.contentType()

    override fun contentLength() = delegate.contentLength()

    override fun source() = progressSource

    /**
     * 下载进度 Source
     */
    private class ProgressSource(
        delegate: Source,
        private val url: String,
        private val totalBytes: Long,
        private val listener: (String, Long, Long) -> Unit,
    ) : ForwardingSource(delegate) {

        private var bytesRead = 0L

        override fun read(sink: Buffer, byteCount: Long): Long {
            val count = super.read(sink, byteCount)
            if (count != -1L) {
                bytesRead += count
            }
            listener(url, bytesRead, totalBytes)
            return count
        }
    }
}
