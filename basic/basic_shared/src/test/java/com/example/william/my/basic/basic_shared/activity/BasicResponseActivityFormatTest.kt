package com.example.william.my.basic.basic_shared.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasicResponseActivityFormatTest {

    @Test
    fun formatJsonFormatsJsonObjectWithoutChangingStringContent() {
        val result = BasicResponseActivity.formatJson(
            """{"message":"a,b{c}","items":[1,2]}"""
        )

        assertTrue(result.contains("\n"))
        assertTrue(result.contains("\"message\": \"a,b{c}\""))
        assertTrue(result.contains("\"items\": ["))
    }

    @Test
    fun formatJsonFormatsJsonArray() {
        val result = BasicResponseActivity.formatJson("""[{"id":1},{"id":2}]""")

        assertTrue(result.contains("\n"))
        assertTrue(result.contains("\"id\": 1"))
    }

    @Test
    fun formatJsonReturnsInvalidJsonUnchanged() {
        val value = """{"message":}"""

        assertEquals(value, BasicResponseActivity.formatJson(value))
    }

    @Test
    fun formatJsonReturnsPlainTextUnchanged() {
        val value = "request completed"

        assertEquals(value, BasicResponseActivity.formatJson(value))
    }
}
