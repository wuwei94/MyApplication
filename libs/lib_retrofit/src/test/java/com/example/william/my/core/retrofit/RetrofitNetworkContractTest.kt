package com.example.william.my.core.retrofit

import com.example.william.my.core.okhttp.closeResources
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.google.gson.JsonParseException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET

class RetrofitNetworkContractTest {
    @Test
    fun converterSupportsDirectObjectsAndCollections() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("""{"name":"Ada"}"""))
            enqueue(MockResponse().setBody("""[{"name":"Ada"},{"name":"Grace"}]"""))
            enqueue(MockResponse().setBody("""[{"name":"Ada"}]"""))
            start()
        }
        val client = okHttpClient { }
        val api = retrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }.create(TestApi::class.java)

        try {
            assertEquals("Ada", api.directUser().execute().body()!!.name)
            assertEquals(listOf("Ada", "Grace"), api.directUsers().execute().body()!!.map(User::name))
            assertEquals("Ada", api.unwrappedUsers().execute().body()!!.data!!.single().name)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun customCodeFieldRejectsNonNumericValues() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"status":"SUCCESS","message":null,"data":{"name":"William"}}""")
            )
            start()
        }
        val client = okHttpClient { }
        val api = retrofit {
            baseUrl(server.url("/").toString())
            client(client)
            code("status")
            message("message")
        }.create(TestApi::class.java)

        try {
            try {
                api.load().execute()
                throw AssertionError("Expected JsonParseException")
            } catch (_: JsonParseException) {
            }
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun converterReturnsTypedResponseFromRealHttpCall() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"errorCode":0,"errorMsg":"","data":{"name":"William"}}""")
            )
            start()
        }
        val client = okHttpClient { }
        val api = retrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }.create(TestApi::class.java)

        try {
            val response = api.load().execute()

            assertEquals(200, response.code())
            assertEquals("William", response.body()!!.data!!.name)
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    @Test
    fun preservesNon2xxErrorBodies() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(503)
                    .setBody("""{"message":"maintenance"}""")
            )
            start()
        }
        val client = okHttpClient { }
        val api = retrofit {
            baseUrl(server.url("/").toString())
            client(client)
        }.create(TestApi::class.java)

        try {
            val missing = api.missing().execute()
            assertFalse(missing.isSuccessful)
            assertEquals(503, missing.code())
            assertEquals("""{"message":"maintenance"}""", missing.errorBody()!!.string())
        } finally {
            client.closeResources()
            server.shutdown()
        }
    }

    private interface TestApi {
        @GET("direct-user")
        fun directUser(): Call<User>

        @GET("direct-users")
        fun directUsers(): Call<List<User>>

        @GET("unwrapped-users")
        fun unwrappedUsers(): Call<RetrofitResponse<List<User>>>

        @GET("typed")
        fun load(): Call<RetrofitResponse<User>>

        @GET("missing")
        fun missing(): Call<RetrofitResponse<User>>
    }

    private data class User(val name: String)
}
