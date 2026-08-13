package com.example.william.my.core.retrofit

import com.example.william.my.core.retrofit.response.RetrofitResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RetrofitResponseTest {
    @Test
    fun successSupportsDataAndNoData() {
        val withData = RetrofitResponse.success("value")
        val withoutData = RetrofitResponse.success<String>()

        assertEquals(RetrofitResponse.SUCCESS, withData.code)
        assertEquals("value", withData.data)
        assertEquals(RetrofitResponse.SUCCESS, withoutData.code)
        assertNull(withoutData.data)
    }
}
