package com.example.william.my.core.retrofit.rx.dynamic

import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.method.Method
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET

class RxDynamicRequestTest {

    @Test
    fun applyDeserializesJsonElementToRequestedType() {
        val response = RetrofitResponse.of<JsonElement>(
            0,
            "",
            JsonParser.parseString("""{"name":"William"}""")
        )

        val converted = RxDynamicResponseFunction<User>(User::class.java).apply(response)

        assertEquals("William", converted.data?.name)
    }

    @Test
    fun rxDynamicRequestBuilderSupportsPatch() {
        val config = RxDynamicRequest.builder<User>()
            .api("user")
            .patch()
            .addJsonBody("""{"name":"William"}""")
            .buildConfig()

        assertEquals(Method.PATCH, config.method)
        assertNotNull(config.requestBody)
    }

    @Test
    fun rxDynamicRequestBuilderAcceptsAnExplicitRetrofitInstance() {
        val retrofit = Retrofit.Builder().baseUrl("https://example.com/").build()
        val config = RxDynamicRequest.builder<User>()
            .api("user")
            .retrofit(retrofit)
            .buildConfig()

        assertEquals(retrofit, config.retrofit)
    }

    @Test
    fun rxDynamicRequestConfigCopiesMutableMaps() {
        val header = mutableMapOf("Authorization" to "token")
        val parameter = mutableMapOf("page" to "1")
        val builder = RxDynamicRequest.builder<User>()
            .api("user")
            .addHeader(header)
            .addParams(parameter)
        val config = builder.buildConfig()

        header["Authorization"] = "changed"
        parameter["page"] = "2"
        builder.addHeader("Later", "value")
        builder.addParam("size", "20")

        assertEquals("token", config.header["Authorization"])
        assertEquals("1", config.parameter["page"])
        assertEquals(null, config.header["Later"])
        assertEquals(null, config.parameter["size"])
    }

    @Test
    fun defaultRxApiCreatesSingleWithoutMissingCallAdapter() {
        val single = createRxApi(TestRxApi::class.java).load()

        assertNotNull(single)
    }

    private interface TestRxApi {
        @GET("https://example.com/value")
        fun load(): io.reactivex.rxjava3.core.Single<RetrofitResponse<JsonElement>>
    }

    private data class User(val name: String)
}
