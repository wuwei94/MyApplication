package com.example.william.my.core.okhttp.body

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer

/**
 * 上传进度请求体
 *
 * 包装原始 [RequestBody] 并在写入过程中回调已写入字节数和总字节数。
 */
class UploadProgressRequestBody(
    private val delegate: RequestBody,
    private val listener: (Long, Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun isOneShot(): Boolean = delegate.isOneShot()

    override fun isDuplex(): Boolean = delegate.isDuplex()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val progressSink = ProgressSink(sink, totalBytes, listener)
        val bufferedSink = progressSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    /**
     * 上传进度 Sink
     */
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
