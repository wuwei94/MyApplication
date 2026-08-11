package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.body.RequestBodyProgressUpload
import com.example.william.my.core.okhttp.body.ResponseBodyProgressDownload
import com.example.william.my.core.okhttp.format.FormatParser
import com.example.william.my.core.okhttp.format.FormatParser.MAX_LOG_BODY_BYTES
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
        val body = RequestBodyProgressUpload(delegate) { _, _ -> }

        assertTrue(body.isOneShot())
        assertTrue(body.isDuplex())
    }

    @Test
    fun responseWrapperReturnsOneSourceAndSurvivesLoggingPreview() {
        val body = ResponseBodyProgressDownload(
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
        val body = ResponseBodyProgressDownload(
            "https://example.com/data",
            delegate
        ) { _, current, _ ->
            currentBytes = current
        }

        body.string()

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
