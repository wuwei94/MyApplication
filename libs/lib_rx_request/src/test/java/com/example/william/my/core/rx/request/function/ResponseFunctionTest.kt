package com.example.william.my.core.rx.request.function

import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseFunctionTest {

    @Test
    fun applyDeserializesJsonElementToRequestedType() {
        val response = RetrofitResponse.of<JsonElement>(
            0,
            "",
            JsonParser.parseString("""{"name":"William"}""")
        )

        val converted = ResponseFunction<User>(User::class.java).apply(response)

        assertEquals("William", converted.data?.name)
    }

    private data class User(val name: String)
}
