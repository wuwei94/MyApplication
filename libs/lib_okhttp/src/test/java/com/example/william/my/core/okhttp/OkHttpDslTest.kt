package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.compat.CompatHttpsSSL
import com.example.william.my.core.okhttp.interceptor.InterceptorLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OkHttpDslTest {

    @Test
    fun formattedLoggingSkipsOneShotRequestBodies() {
        val body = object : RequestBody() {
            override fun contentType() = "application/json".toMediaType()

            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8("payload")
            }

            override fun isOneShot(): Boolean = true
        }

        assertFalse(InterceptorLogging(emptyList()).isSafeToLog(body))
    }

    @Test
    fun formattedLoggingSkipsOversizedRequestBodies() {
        val body = object : RequestBody() {
            override fun contentType() = "application/json".toMediaType()
            override fun contentLength() = 1024L * 1024L + 1L
            override fun writeTo(sink: BufferedSink) = Unit
        }

        assertFalse(InterceptorLogging(emptyList()).isSafeToLog(body))
    }

    @Test
    fun sslBypassGuardRejectsReleaseMode() {
        try {
            CompatHttpsSSL.requireDebugSslBypass(debuggable = false)
            fail("Release builds must reject ignoreSSL()")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("debug builds"))
        }
    }

    @Test
    fun clientFactoryReturnsCallerOwnedInstances() {
        val first = okHttpClient { timeout(5) }
        val second = okHttpClient { timeout(5) }

        try {
            assertNotSame(first, second)
        } finally {
            first.closeResources()
            second.closeResources()
        }
    }

    @Test
    fun cachedClientReusesNamedInstance() {
        val name = "okhttp-dsl-test"
        val first = cachedClient(name) { timeout(5) }

        try {
            val second = cachedClient(name) { timeout(10) }
            assertSame(first, second)
            assertSame(first, getCachedClient(name))
        } finally {
            removeCachedClient(name)
        }
    }

    @Test
    fun removingCachedClientClosesItsResources() {
        val name = "okhttp-dsl-remove-test"
        val client = cachedClient(name) { timeout(5) }

        val removed = removeCachedClient(name)

        assertSame(client, removed)
        assertTrue(client.dispatcher.executorService.isShutdown)
        assertNull(removeCachedClient(name))
    }

    @Test
    fun clearingCachedClientsClosesAllResources() {
        val firstName = "okhttp-dsl-clear-first-test"
        val secondName = "okhttp-dsl-clear-second-test"
        val first = cachedClient(firstName) { timeout(5) }
        val second = cachedClient(secondName) { timeout(5) }

        clearCachedClients()

        assertTrue(first.dispatcher.executorService.isShutdown)
        assertTrue(second.dispatcher.executorService.isShutdown)
        assertNull(removeCachedClient(firstName))
        assertNull(removeCachedClient(secondName))
    }
}
