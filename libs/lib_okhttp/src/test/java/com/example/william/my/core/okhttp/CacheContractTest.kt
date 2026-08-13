package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.header.ControlHeaders
import com.example.william.my.core.okhttp.interceptor.InterceptorCacheRequest
import com.example.william.my.core.okhttp.interceptor.InterceptorCacheResponse
import okhttp3.Cache
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CacheContractTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun offlineRequestUsesPreviouslyCachedResponse() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("cached"))
            start()
        }
        var connected = true
        val client = okHttpClient {
            raw {
                cache(Cache(temporaryFolder.newFolder("http-cache"), 1024L * 1024L))
                addInterceptor(InterceptorCacheRequest { connected })
                addNetworkInterceptor(InterceptorCacheResponse())
            }
        }
        val request = Request.Builder()
            .url(server.url("/cache"))
            .header(ControlHeaders.CACHE_ALIVE_SECONDS, "60")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                assertEquals("cached", response.body.string())
            }
            assertEquals(
                null,
                server.takeRequest().getHeader(ControlHeaders.CACHE_ALIVE_SECONDS)
            )

            connected = false
            client.newCall(request).execute().use { response ->
                assertEquals("cached", response.body.string())
                assertNotNull(response.cacheResponse)
            }
            assertEquals(1, server.requestCount)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }
}
