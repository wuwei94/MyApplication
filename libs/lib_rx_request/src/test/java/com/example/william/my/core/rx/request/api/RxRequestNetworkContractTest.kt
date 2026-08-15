package com.example.william.my.core.rx.request.api

import com.example.william.my.core.okhttp.closeResources
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.rx.request.RxRequest
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.google.gson.JsonElement
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class RxRequestNetworkContractTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun defaultRxApiCreatesSingleWithoutMissingCallAdapter() {
        val single = createRxApi(TestRxApi::class.java).load()

        assertNotNull(single)
    }

    @Test
    fun buildSingleRequiresAnApiPath() {
        try {
            RxRequest.builder<User>().buildSingle()
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
            val response = RxRequest.builder<User>()
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
            RxRequest.builder<User>()
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

    @Test
    fun multipartPostSendsMultipartBody() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse().setBody(
                    """{"errorCode":0,"errorMsg":"","data":{"uploaded":true}}"""
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
            val response = RxRequest.builder<JsonElement>()
                .api("upload")
                .post()
                .addMultipartField("source", "lib_rx_request")
                .retrofit(retrofit)
                .observeOn(Schedulers.trampoline())
                .buildSingle()
                .blockingGet()

            val request = server.takeRequest()
            assertTrue(request.getHeader("Content-Type").orEmpty().startsWith("multipart/form-data"))
            assertTrue(request.body.readUtf8().contains("lib_rx_request"))
            assertEquals(true, response.data?.asJsonObject?.get("uploaded")?.asBoolean)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun multipartPutSendsFieldsAndFile() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse().setBody(
                    """{"errorCode":0,"errorMsg":"","data":{"uploaded":true}}"""
                )
            )
            start()
        }
        val client = okHttpClient { }
        val retrofit = rxRetrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }
        val file = temporaryFolder.newFile("profile.txt").apply {
            writeText("profile-content")
        }

        try {
            RxRequest.builder<JsonElement>()
                .api("profile")
                .put()
                .addMultipartFields(
                    mapOf(
                        "name" to "William",
                        "source" to "android",
                    )
                )
                .addFile("profile", file)
                .retrofit(retrofit)
                .observeOn(Schedulers.trampoline())
                .buildSingle()
                .blockingGet()

            val request = server.takeRequest()
            val body = request.body.readUtf8()
            assertEquals("PUT", request.method)
            assertTrue(request.getHeader("Content-Type").orEmpty().startsWith("multipart/form-data"))
            assertTrue(body.contains("name=\"name\""))
            assertTrue(body.contains("William"))
            assertTrue(body.contains("filename=\"profile.txt\""))
            assertTrue(body.contains("Content-Type: application/octet-stream"))
            assertTrue(body.contains("profile-content"))
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    private data class User(val name: String)

    private interface TestRxApi {
        @GET("https://example.com/value")
        fun load(): io.reactivex.rxjava3.core.Single<RetrofitResponse<JsonElement>>
    }
}
