package com.example.william.my.core.ktor

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class KtorNetworkContractTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun connectionRetryDoesNotRetryHttpFailure() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(503).setBody("unavailable"))
            start()
        }
        val client = ktorClient { }

        try {
            runCatching {
                client.get(server.url("/default-retry").toString()).bodyAsText()
            }
            assertEquals(1, server.requestCount)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun httpCacheServesAReusableResponseWithoutAnotherNetworkCall() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setHeader("Cache-Control", "public, max-age=60")
                    .setBody("cached")
            )
            start()
        }
        val client = ktorClient {
            cache(temporaryFolder.newFolder("http-cache"))
        }

        try {
            val url = server.url("/cached").toString()
            assertEquals("cached", client.get(url).bodyAsText())
            assertEquals("cached", client.get(url).bodyAsText())
            assertEquals(1, server.requestCount)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun customCookieStorageSuppliesRequestCookies() = runBlocking {
        val storage = object : CookiesStorage {
            override suspend fun get(requestUrl: Url): List<Cookie> {
                return listOf(Cookie(name = "session", value = "stored"))
            }

            override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = Unit

            override fun close() = Unit
        }
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        val client = ktorClient { cookies(storage) }

        try {
            assertEquals("ok", client.get(server.url("/cookie").toString()).bodyAsText())
            assertEquals("session=stored", server.takeRequest().getHeader("Cookie"))
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun redirectsAreEnabledByDefault() = runBlocking {
        val target = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200).setBody("target"))
            start()
        }
        val origin = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", target.url("/target"))
            )
            start()
        }
        val client = ktorClient { }

        try {
            assertEquals(
                "target",
                client.get(origin.url("/start").toString()).bodyAsText()
            )
            assertEquals(1, target.requestCount)
        } finally {
            client.close()
            origin.shutdown()
            target.shutdown()
        }
    }
}
