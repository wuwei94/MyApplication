package com.example.william.my.core.retrofit.rx.dynamic

import com.example.william.my.core.okhttp.closeResources
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxDynamicRequestNetworkContractTest {
    @Test
    fun buildSingleRequiresAnApiPath() {
        try {
            RxDynamicRequest.builder<User>().buildSingle()
            fail("Expected IllegalStateException")
        } catch (error: IllegalStateException) {
            assertEquals(
                "Request API must be configured with api(...) before buildSingle()",
                error.message
            )
        }
    }

    @Test
    fun realHttpCallReturnsTypedData() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse().setBody(
                    """{"errorCode":0,"errorMsg":"","data":{"name":"William"}}"""
                )
            )
            start()
        }
        val client = okHttpClient { }
        val retrofit = rxRetrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }

        try {
            val response = RxDynamicRequest.builder<User>()
                .api("user")
                .get()
                .retrofit(retrofit)
                .observeOn(Schedulers.trampoline())
                .buildSingle()
                .blockingGet()

            assertEquals("William", response.data?.name)
            assertEquals(1, server.requestCount)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun non2xxResponseKeepsStatusCodeAndMessage() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(401)
                    .setBody("""{"message":"expired"}""")
            )
            start()
        }
        val client = okHttpClient { }
        val retrofit = rxRetrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }

        try {
            RxDynamicRequest.builder<User>()
                .api("protected")
                .post()
                .addParam("name", "William")
                .retrofit(retrofit)
                .observeOn(Schedulers.trampoline())
                .buildSingle()
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError { error ->
                    error is ApiException &&
                        error.code == 401 &&
                        error.message == "expired"
                }

            assertEquals(1, server.requestCount)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    private data class User(val name: String)
}
