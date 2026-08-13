package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.body.DownloadProgressResponseBody
import com.example.william.my.core.okhttp.body.RequestBodyProgress
import com.example.william.my.core.okhttp.body.ResponseBodyProgress
import com.example.william.my.core.okhttp.body.UploadProgressRequestBody
import com.example.william.my.core.okhttp.format.FormatParser
import com.example.william.my.core.okhttp.format.FormatParser.MAX_LOG_BODY_BYTES
import com.example.william.my.core.okhttp.listener.RequestProgressListener
import com.example.william.my.core.okhttp.listener.ResponseProgressListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.BufferedSink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressBodyTest {

    @Test
    fun requestWrapperKeepsTransmissionSemantics() {
        val delegate = object : RequestBody() {
            override fun contentType() = "application/json".toMediaType()
            override fun isOneShot() = true
            override fun isDuplex() = true
            override fun writeTo(sink: BufferedSink) = Unit
        }
        val body = UploadProgressRequestBody(delegate) { _, _ -> }

        assertTrue(body.isOneShot())
        assertTrue(body.isDuplex())
    }

    @Test
    fun responseWrapperReturnsOneSourceAndSurvivesLoggingPreview() {
        val body = DownloadProgressResponseBody(
            url = "https://example.com/data",
            delegate = "payload".toResponseBody("text/plain".toMediaType())
        ) { _, _, _ -> }
        val response = response(body)

        assertSame(body.source(), body.source())
        assertEquals("payload", FormatParser.parseResponse(response))
        assertEquals("payload", response.body.string())
    }

    @Test
    fun unknownLengthResponseKeepsActualReadCountAtEof() {
        val delegate = object : ResponseBody() {
            private val source = Buffer().writeUtf8("payload")
            override fun contentType() = "text/plain".toMediaType()
            override fun contentLength() = -1L
            override fun source() = source
        }
        var currentBytes = 0L
        val body = DownloadProgressResponseBody(
            "https://example.com/data",
            delegate
        ) { _, current, _ ->
            currentBytes = current
        }

        body.string()

        assertEquals(7L, currentBytes)
    }

    @Suppress("DEPRECATION")
    @Test
    fun deprecatedRequestBodyDelegatesToUploadProgressBody() {
        val delegate = object : RequestBody() {
            override fun contentType() = "text/plain".toMediaType()
            override fun contentLength() = 7L
            override fun isOneShot() = true
            override fun isDuplex() = true
            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8("payload")
            }
        }
        var currentBytes = 0L
        val body = RequestBodyProgress(
            mRequestBody = delegate,
            mRequestProgressListener = object : RequestProgressListener {
                override fun onProgress(currentSize: Long, totalSize: Long) {
                    currentBytes = currentSize
                }
            }
        )

        body.writeTo(Buffer())

        assertTrue(body.isOneShot())
        assertTrue(body.isDuplex())
        assertEquals(7L, currentBytes)
    }

    @Suppress("DEPRECATION")
    @Test
    fun deprecatedResponseBodyDelegatesToDownloadProgressBody() {
        var currentBytes = 0L
        val body = ResponseBodyProgress(
            mUrl = "https://example.com/data",
            mResponseBody = "payload".toResponseBody("text/plain".toMediaType()),
            mResponseProgressListener = object : ResponseProgressListener {
                override fun onProgress(url: String, currentSize: Long, totalSize: Long) {
                    currentBytes = currentSize
                }
            }
        )

        assertEquals("payload", body.string())
        assertEquals(7L, currentBytes)
    }

    @Test
    fun responseLoggingIsLimitedWithoutConsumingBody() {
        val payload = "x".repeat(MAX_LOG_BODY_BYTES.toInt() + 10)
        val response = response(payload.toResponseBody("text/plain".toMediaType()))

        val preview = FormatParser.parseResponse(response)

        assertTrue(preview.contains("Body omitted"))
        assertEquals(payload, response.body.string())
    }

    private fun response(body: ResponseBody): Response {
        return Response.Builder()
            .request(Request.Builder().url("https://example.com/data").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body)
            .build()
    }
}
