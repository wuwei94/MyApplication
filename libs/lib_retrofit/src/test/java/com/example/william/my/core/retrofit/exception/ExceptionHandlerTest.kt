package com.example.william.my.core.retrofit.exception

import com.google.gson.JsonParseException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class ExceptionHandlerTest {

    @Test
    fun httpExceptionUsesStatusCodeAndServerMessage() {
        val cause = httpException(503, """{"message":"temporarily unavailable"}""")

        val exception = ExceptionHandler.handleException(cause)

        assertEquals(503, exception.code)
        assertEquals("temporarily unavailable", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    fun httpExceptionSupportsKnownMessageKeys() {
        assertEquals(
            "rate limited",
            ExceptionHandler.handleException(
                httpException(429, """{"msg":"rate limited"}""")
            ).message
        )
        assertEquals(
            "forbidden",
            ExceptionHandler.handleException(
                httpException(403, """{"errorMsg":"forbidden"}""")
            ).message
        )
    }

    @Test
    fun httpExceptionUsesBodyOrStatusFallback() {
        assertEquals(
            "Internal Server Error",
            ExceptionHandler.handleException(
                httpException(500, "Internal Server Error", "text/plain")
            ).message
        )
        assertEquals(
            "请求错误(422)",
            ExceptionHandler.handleException(httpException(422, "{}")).message
        )
        assertEquals(
            "请求错误(500)",
            ExceptionHandler.handleException(httpException(500, "   ")).message
        )
    }

    @Test
    fun serverResultExceptionUsesBusinessCode() {
        val cause = ServerResultException(code = 2001, message = "业务失败")

        val exception = ExceptionHandler.handleException(cause)

        assertEquals(2001, exception.code)
        assertEquals("业务失败", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test(expected = CancellationException::class)
    fun cancellationExceptionIsRethrown() {
        ExceptionHandler.handleException(CancellationException())
    }

    @Test
    fun connectionFailuresUseConnectError() {
        listOf(
            ConnectException("Connection refused"),
            UnknownHostException("unknown host")
        ).forEach { cause ->
            val exception = ExceptionHandler.handleException(cause)

            assertEquals(ApiException.Error.CONNECT_ERROR, exception.code)
            assertSame(cause, exception.cause)
        }
    }

    @Test
    fun timeoutUsesTimeoutError() {
        val cause = SocketTimeoutException("timeout")

        val exception = ExceptionHandler.handleException(cause)

        assertEquals(ApiException.Error.TIMEOUT_ERROR, exception.code)
        assertEquals("请求超时，请稍后再试", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    fun sslFailuresUseSslError() {
        listOf(
            SSLHandshakeException("handshake failed"),
            SSLPeerUnverifiedException("pinning failed")
        ).forEach { cause ->
            val exception = ExceptionHandler.handleException(cause)

            assertEquals(ApiException.Error.SSL_ERROR, exception.code)
            assertSame(cause, exception.cause)
        }
    }

    @Test
    fun parseFailuresUseParseError() {
        listOf(
            JsonParseException("bad json"),
            JSONException("unexpected token")
        ).forEach { cause ->
            val exception = ExceptionHandler.handleException(cause)

            assertEquals(ApiException.Error.PARSE_ERROR, exception.code)
            assertEquals("解析错误，请稍后再试", exception.message)
            assertSame(cause, exception.cause)
        }
    }

    @Test
    fun unknownFailureUsesUnknownErrorAndCauseMessage() {
        val cause = RuntimeException("something went wrong")

        val exception = ExceptionHandler.handleException(cause)

        assertEquals(ApiException.Error.UNKNOWN, exception.code)
        assertEquals("something went wrong", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    fun genericIOExceptionUsesUnknownErrorAndKeepsCauseMessage() {
        val cause = IOException("broken pipe")

        val exception = ExceptionHandler.handleException(cause)

        assertEquals(ApiException.Error.UNKNOWN, exception.code)
        assertEquals("broken pipe", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    fun blankCauseMessageUsesApiExceptionDefaultMessage() {
        val direct = ApiException(RuntimeException(""), ApiException.Error.UNKNOWN)
        val handled = ExceptionHandler.handleException(RuntimeException(" "))

        assertEquals(ApiException.DEFAULT_MESSAGE, direct.message)
        assertEquals(ApiException.DEFAULT_MESSAGE, handled.message)
    }

    private fun httpException(
        code: Int,
        body: String,
        mediaType: String = "application/json"
    ): HttpException {
        return HttpException(
            Response.error<Any>(
                code,
                body.toResponseBody(mediaType.toMediaType())
            )
        )
    }
}
