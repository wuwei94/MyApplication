package com.example.william.my.core.ktor

import com.example.william.my.core.ktor.request.postFormResult
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorEngineTest {

    @Test
    fun okHttpEngineExecutesRealRequest() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("OKHTTP"))
            start()
        }
        val client = ktorClient { }

        try {
            assertEquals(
                "OKHTTP",
                client.get(server.url("/engine").toString()).bodyAsText()
            )
            assertEquals(1, server.requestCount)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun defaultEngineInheritsInjectedOkHttpInterceptors() = runBlocking {
        val interceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("X-Injected-Client", "true")
                    .build()
            )
        }
        val injected = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        val client = ktorClient {
            client(injected)
        }

        try {
            assertEquals(
                "ok",
                client.get(server.url("/injected").toString()).bodyAsText()
            )
            assertEquals("true", server.takeRequest().getHeader("X-Injected-Client"))
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun clientRequestExtensionSendsFormAndReturnsTypedResult() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("accepted"))
            start()
        }
        val client = ktorClient { }

        try {
            val result = client.postFormResult<String>(
                url = server.url("/form").toString(),
                params = mapOf("username" to "william"),
                headers = mapOf("X-Request" to "extension")
            )

            assertEquals("accepted", result.getOrThrow())
            val request = server.takeRequest()
            assertEquals("username=william", request.body.readUtf8())
            assertEquals("extension", request.getHeader("X-Request"))
        } finally {
            client.close()
            server.shutdown()
        }
    }

}
