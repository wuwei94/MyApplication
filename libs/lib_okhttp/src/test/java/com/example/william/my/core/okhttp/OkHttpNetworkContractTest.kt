package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.cookie.MemoryCookieStore
import com.example.william.my.core.okhttp.interceptor.InterceptorBaseUrl
import com.example.william.my.core.okhttp.interceptor.InterceptorLogging
import okhttp3.Cookie
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.BufferedSink
import org.junit.Assert.assertEquals
import org.junit.Test

class OkHttpNetworkContractTest {
    @Test
    fun storedCookiesMergeWithoutReplacingCallerCookies() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        val store = MemoryCookieStore()
        store.save(
            server.url("/"),
            listOf(
                Cookie.Builder()
                    .name("manual")
                    .value("stored")
                    .domain(server.hostName)
                    .path("/")
                    .build(),
                Cookie.Builder()
                    .name("session")
                    .value("stored")
                    .domain(server.hostName)
                    .path("/")
                    .build()
            )
        )
        val client = okHttpClient { cookieJar(store) }

        try {
            client.newCall(
                Request.Builder()
                    .url(server.url("/cookies"))
                    .header("Cookie", "manual=caller")
                    .build()
            ).execute().close()

            assertEquals("manual=caller; session=stored", server.takeRequest().getHeader("Cookie"))
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun filteredLoggingDoesNotReadRequestBodyBeforeSending() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        var writeCount = 0
        val body = object : RequestBody() {
            override fun contentType() = "application/json".toMediaType()
            override fun contentLength() = 7L
            override fun writeTo(sink: BufferedSink) {
                writeCount++
                sink.writeUtf8("payload")
            }
        }
        val client = okHttpClient {
            addInterceptor(InterceptorLogging(listOf("/filtered")))
        }

        try {
            client.newCall(
                Request.Builder()
                    .url(server.url("/filtered?source=test"))
                    .post(body)
                    .build()
            ).execute().close()

            assertEquals(1, writeCount)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun baseUrlRedirectKeepsOriginalPathAndQuery() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        val client = okHttpClient { addInterceptor(InterceptorBaseUrl()) }

        try {
            client.newCall(
                Request.Builder()
                    .url(server.url("/original/path?source=test"))
                    .header("Retrofit-Url-Redirect", server.url("/ignored").toString())
                    .build()
            ).execute().close()

            assertEquals("/original/path?source=test", server.takeRequest().path)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun callerCookieIsNotForwardedToRedirectedHost() {
        val redirectServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        val originServer = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .addHeader(
                        "Location",
                        redirectServer.url("/target").newBuilder().host("127.0.0.1").build()
                    )
            )
            start()
        }
        val client = okHttpClient { cookieJar() }

        try {
            client.newCall(
                Request.Builder()
                    .url(originServer.url("/redirect"))
                    .header("Cookie", "session=caller")
                    .build()
            ).execute().close()

            assertEquals("session=caller", originServer.takeRequest().getHeader("Cookie"))
            assertEquals(null, redirectServer.takeRequest().getHeader("Cookie"))
        } finally {
            client.closeResources()
            originServer.shutdown()
            redirectServer.shutdown()
        }
    }

}
