package com.example.william.my.core.okhttp.body

import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.buffer

/**
 * 下载进度包装，包装 [ResponseBody] 实现读取进度监听。
 */
class ProgressResponseBody(
    private val url: String,
    private val delegate: ResponseBody,
    private val listener: (url: String, Long, Long) -> Unit
) : ResponseBody() {

    override fun contentType() = delegate.contentType()

    override fun contentLength() = delegate.contentLength()

    override fun source() = ProgressSource(delegate.source(), url, contentLength(), listener).buffer()

    private class ProgressSource(
        delegate: Source,
        private val url: String,
        private val totalBytes: Long,
        private val listener: (String, Long, Long) -> Unit
    ) : ForwardingSource(delegate) {

        private var bytesRead = 0L

        override fun read(sink: Buffer, byteCount: Long): Long {
            val count = super.read(sink, byteCount)
            if (count == -1L) {
                bytesRead = totalBytes
            } else {
                bytesRead += count
            }
            listener(url, bytesRead, totalBytes)
            return count
        }
    }
}
