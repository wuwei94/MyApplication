package com.example.william.my.core.okhttp.body

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer

/**
 * 上传进度包装，包装 [RequestBody] 实现写入进度监听。
 */
class ProgressRequestBody(
    private val delegate: RequestBody,
    private val listener: (Long, Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val progressSink = ProgressSink(sink, totalBytes, listener)
        val bufferedSink = progressSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private class ProgressSink(
        delegate: Sink,
        private val totalBytes: Long,
        private val listener: (Long, Long) -> Unit
    ) : ForwardingSink(delegate) {

        private var bytesWritten = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener(bytesWritten, totalBytes)
        }
    }
}
