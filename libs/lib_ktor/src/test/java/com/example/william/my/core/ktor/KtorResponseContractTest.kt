package com.example.william.my.core.ktor

import com.example.william.my.core.ktor.exception.ApiException
import com.example.william.my.core.ktor.request.getResponse
import com.example.william.my.core.ktor.request.getResult
import com.example.william.my.core.ktor.response.KtorResponse
import com.google.gson.FieldNamingPolicy
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorResponseContractTest {
    @Test
    fun responseFactoriesMatchRetrofitResponseContract() {
        val loading = KtorResponse.loading<User>()
        val success = KtorResponse.success(User("William"))
        val successWithoutData = KtorResponse.success<User>()
        val error = KtorResponse.error<User>("failed")
        val custom = KtorResponse.of(10, "custom", User("Ada"))

        assertEquals(KtorResponse.LOADING, loading.code)
        assertEquals(KtorResponse.SUCCESS, success.code)
        assertEquals("William", success.data?.name)
        assertEquals(KtorResponse.SUCCESS, successWithoutData.code)
        assertNull(successWithoutData.data)
        assertEquals(KtorResponse.ERROR, error.code)
        assertEquals("failed", error.message)
        assertEquals(10, custom.code)
        assertEquals("Ada", custom.data?.name)
    }

    @Test
    fun businessEnvelopeReturnsTypedData() = runBlocking {
        val server = serverWith(
            """{"errorCode":0,"errorMsg":"ok","data":{"name":"William"}}"""
        )
        val client = ktorClient { }

        try {
            val response = client.getResponse<User>(server.url("/user").toString()).getOrThrow()
            assertEquals(0, response.code)
            assertTrue(response.isSuccess)
            assertEquals("ok", response.message)
            assertEquals("William", response.data?.name)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun responseWithoutEnvelopeIsWrappedAsSuccess() = runBlocking {
        val server = serverWith("""{"name":"William"}""")
        val client = ktorClient { }

        try {
            val response = client.getResponse<User>(server.url("/user").toString()).getOrThrow()
            assertEquals(0, response.code)
            assertEquals("William", response.data?.name)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun businessFailureIsReturnedForCallerInspection() = runBlocking {
        val body = """{"errorCode":40101,"errorMsg":"token expired","data":null}"""
        val server = serverWith(body)
        val client = ktorClient { }

        try {
            val response = client.getResponse<User>(server.url("/user").toString()).getOrThrow()
            assertEquals(40101, response.code)
            assertEquals("token expired", response.message)
            assertFalse(response.isSuccess)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun httpFailureUsesStatusCodeAndServerMessage() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(503)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"message":"temporarily unavailable"}""")
            )
            start()
        }
        val client = ktorClient { }

        try {
            val error = client.getResponse<User>(server.url("/unavailable").toString())
                .exceptionOrNull() as ApiException
            assertEquals(503, error.code)
            assertEquals("temporarily unavailable", error.message)
            assertTrue(error.cause is ResponseException)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun invalidBusinessCodeIsReportedAsParseError() = runBlocking {
        val server = serverWith("""{"errorCode":"invalid","errorMsg":"bad","data":null}""")
        val client = ktorClient { }

        try {
            val error = client.getResponse<User>(server.url("/user").toString())
                .exceptionOrNull() as ApiException
            assertEquals(ApiException.Error.PARSE_ERROR, error.code)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun customCodeAndMessageFieldsMatchRetrofitConfiguration() = runBlocking {
        val server = serverWith("""{"code":0,"message":"ok","data":{"name":"William"}}""")
        val client = ktorClient {
            code("code")
            message("message")
        }

        try {
            val response = client.getResponse<User>(server.url("/user").toString()).getOrThrow()
            assertEquals(0, response.code)
            assertEquals("ok", response.message)
            assertEquals("William", response.data?.name)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun baseUrlResolvesRelativeRequestPath() = runBlocking {
        val server = serverWith("""{"name":"William"}""")
        val client = ktorClient { baseUrl(server.url("/api/").toString()) }

        try {
            val response = client.getResponse<User>("user").getOrThrow()
            assertEquals("William", response.data?.name)
            assertEquals("/api/user", server.takeRequest().path)
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun gsonConfigurationIsSharedByDirectAndBusinessResponses() = runBlocking {
        val body = """{"display_name":"William"}"""
        val server = MockWebServer().apply {
            enqueue(jsonResponse(body))
            enqueue(jsonResponse(body))
            start()
        }
        val client = ktorClient {
            gson { setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) }
        }

        try {
            assertEquals(
                "William",
                client.getResult<UserProfile>(server.url("/direct").toString())
                    .getOrThrow()
                    .displayName
            )
            assertEquals(
                "William",
                client.getResponse<UserProfile>(server.url("/business").toString())
                    .getOrThrow()
                    .data
                    ?.displayName
            )
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun rawAppliesAdditionalKtorConfiguration() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(500).setBody("error"))
            start()
        }
        val client = ktorClient { raw { expectSuccess = false } }

        try {
            assertEquals("error", client.get(server.url("/error").toString()).bodyAsText())
        } finally {
            client.close()
            server.shutdown()
        }
    }

    @Test
    fun emptySuccessResponsesSupportUnitAndNullableData() = runBlocking {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(204))
            enqueue(MockResponse().setResponseCode(205))
            enqueue(MockResponse().setResponseCode(200).setBody(""))
            start()
        }
        val client = ktorClient { }

        try {
            assertSame(
                Unit,
                client.getResponse<Unit>(server.url("/no-content").toString())
                    .getOrThrow()
                    .data
            )
            assertSame(
                Unit,
                client.getResponse<Unit>(server.url("/reset-content").toString())
                    .getOrThrow()
                    .data
            )
            assertNull(
                client.getResponse<User>(server.url("/empty").toString())
                    .getOrThrow()
                    .data
            )
        } finally {
            client.close()
            server.shutdown()
        }
    }

    private fun serverWith(body: String): MockWebServer {
        return MockWebServer().apply {
            enqueue(jsonResponse(body))
            start()
        }
    }

    private fun jsonResponse(body: String): MockResponse {
        return MockResponse().setHeader("Content-Type", "application/json").setBody(body)
    }

    private data class User(val name: String)
    private data class UserProfile(val displayName: String)
}
