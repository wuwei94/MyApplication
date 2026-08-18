package com.example.william.my.core.rx.request.builder

import com.example.william.my.core.rx.request.RxRequest
import com.example.william.my.core.rx.request.method.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit

class RxRequestBuilderTest {

    @Test
    fun rxRequestBuilderSupportsPatch() {
        val config = RxRequest.builder<User>()
            .api("user")
            .patch()
            .addJsonBody("""{"name":"William"}""")
            .buildConfig()

        assertEquals(HttpMethod.PATCH, config.method)
        assertNotNull(config.requestBody)
    }

    @Test
    fun rxRequestBuilderAcceptsAnExplicitRetrofitInstance() {
        val retrofit = Retrofit.Builder().baseUrl("https://example.com/").build()
        val config = RxRequest.builder<User>()
            .api("user")
            .retrofit(retrofit)
            .buildConfig()

        assertEquals(retrofit, config.retrofit)
    }

    @Test
    fun rxRequestConfigCopiesMutableMaps() {
        val header = mutableMapOf("Authorization" to "token")
        val parameter = mutableMapOf("page" to "1")
        val builder = RxRequest.builder<User>()
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
    fun multipartRejectsDelete() {
        assertThrows(IllegalArgumentException::class.java) {
            RxRequest.builder<User>()
                .api("user")
                .delete()
                .addMultipartField("name", "William")
                .buildConfig()
        }
    }

    @Test
    fun getRejectsRequestBody() {
        assertThrows(IllegalArgumentException::class.java) {
            RxRequest.builder<User>()
                .api("user")
                .get()
                .addJsonBody("""{"name":"William"}""")
                .buildConfig()
        }
    }

    @Test
    fun latestBodyModeReplacesPreviousMode() {
        val config = RxRequest.builder<User>()
            .api("user")
            .addJsonBody("""{"name":"old"}""")
            .addMultipartField("name", "William")
            .post()
            .buildConfig()

        assertEquals(null, config.requestBody)
        assertNotNull(config.multipartBody)
    }

    @Test
    fun multipartFieldsSupportPut() {
        val config = RxRequest.builder<User>()
            .api("user")
            .put()
            .addMultipartFields(
                mapOf(
                    "name" to "William",
                    "source" to "android",
                )
            )
            .buildConfig()

        assertEquals(HttpMethod.PUT, config.method)
        assertNotNull(config.multipartBody)
    }

    @Test
    fun addHeadersMergesIncrementallyAndSetHeadersReplaces() {
        val config = RxRequest.builder<User>()
            .api("user")
            .addHeader("A", "1")
            .addHeaders(mapOf("B" to "2"))
            .addParam("p1", "v1")
            .addParams(mapOf("p2" to "v2"))
            .buildConfig()

        assertEquals(mapOf("A" to "1", "B" to "2"), config.header)
        assertEquals(mapOf("p1" to "v1", "p2" to "v2"), config.parameter)

        val resetConfig = RxRequest.builder<User>()
            .api("user")
            .addHeader("A", "1")
            .setHeaders(mapOf("C" to "3"))
            .addParam("p1", "v1")
            .setParams(mapOf("p3" to "v3"))
            .buildConfig()

        assertEquals(mapOf("C" to "3"), resetConfig.header)
        assertEquals(mapOf("p3" to "v3"), resetConfig.parameter)
    }

    private data class User(val name: String)
}
