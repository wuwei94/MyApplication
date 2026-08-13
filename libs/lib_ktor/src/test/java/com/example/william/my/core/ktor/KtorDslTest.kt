package com.example.william.my.core.ktor

import com.example.william.my.core.ktor.builder.KtorClientBuilder
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KtorDslTest {
    @Test
    fun clientFactoryReturnsCallerOwnedInstances() {
        val first = ktorClient { timeout(5) }
        val second = ktorClient { timeout(5) }

        try {
            assertNotSame(first, second)
        } finally {
            first.close()
            second.close()
        }
    }

    @Test
    fun timeoutMustBePositive() {
        try {
            ktorClient { timeout(0) }
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("greater than zero"))
        }
    }

    @Test
    fun individualTimeoutsMustBePositive() {
        listOf<(KtorClientBuilder) -> Unit>(
            { it.requestTimeout(0) },
            { it.connectTimeout(0) },
            { it.socketTimeout(0) }
        ).forEach { configure ->
            try {
                configure(KtorClientBuilder())
                fail("Expected IllegalArgumentException")
            } catch (error: IllegalArgumentException) {
                assertTrue(error.message!!.contains("greater than zero"))
            }
        }
    }

    @Test
    fun baseUrlMustBeAbsoluteAndEndWithSlash() {
        listOf(
            "relative/",
            "https://example.com/api",
            "https://example.com/api/?page=1",
            "https://example.com/api/#section"
        ).forEach { invalidUrl ->
            try {
                KtorClientBuilder().baseUrl(invalidUrl)
                fail("Expected IllegalArgumentException")
            } catch (_: IllegalArgumentException) {
                // 预期行为：避免相对地址或路径合并语义不明确的基础地址。
            }
        }
    }
}
