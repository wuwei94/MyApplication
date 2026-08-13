package com.example.william.my.core.ktor

import com.example.william.my.core.ktor.exception.ApiException
import com.example.william.my.core.ktor.exception.ExceptionHandler
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException

class KtorSafetyTest {
    @Test(expected = CancellationException::class)
    fun exceptionHandlerRethrowsCancellation() {
        runBlocking {
            ExceptionHandler.handleException(CancellationException("cancelled"))
        }
    }

    @Test
    fun jsonConvertExceptionMapsToParse() = runBlocking {
        val cause = JsonConvertException("bad json")
        val error = ExceptionHandler.handleException(cause)
        assertEquals(ApiException.Error.PARSE_ERROR, error.code)
        assertSame(cause, error.cause)
    }

    @Test
    fun sslPeerVerificationMapsToSsl() = runBlocking {
        val cause = SSLPeerUnverifiedException("pinning failed")
        val error = ExceptionHandler.handleException(cause)
        assertEquals(ApiException.Error.SSL_ERROR, error.code)
        assertSame(cause, error.cause)
    }

    @Test
    fun genericIOExceptionUsesUnknownError() = runBlocking {
        val cause = IOException("broken pipe")
        val error = ExceptionHandler.handleException(cause)

        assertEquals(ApiException.Error.UNKNOWN, error.code)
        assertEquals("broken pipe", error.message)
        assertSame(cause, error.cause)
    }
}
