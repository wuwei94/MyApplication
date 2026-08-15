package com.example.william.my.basic.basic_shared.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonFormatterTest {

    @Test
    fun formatFormatsJsonObjectWithoutChangingStringContent() {
        val result = JsonFormatter.format(
            """{"message":"a,b{c}","items":[1,2]}"""
        )

        assertTrue(result.contains("\n"))
        assertTrue(result.contains("\"message\": \"a,b{c}\""))
        assertTrue(result.contains("\"items\": ["))
    }

    @Test
    fun formatFormatsJsonArray() {
        val result = JsonFormatter.format("""[{"id":1},{"id":2}]""")

        assertTrue(result.contains("\n"))
        assertTrue(result.contains("\"id\": 1"))
    }

    @Test
    fun formatReturnsInvalidJsonUnchanged() {
        val value = """{"message":}"""

        assertEquals(value, JsonFormatter.format(value))
    }

    @Test
    fun formatReturnsPlainTextUnchanged() {
        val value = "request completed"

        assertEquals(value, JsonFormatter.format(value))
    }
}
