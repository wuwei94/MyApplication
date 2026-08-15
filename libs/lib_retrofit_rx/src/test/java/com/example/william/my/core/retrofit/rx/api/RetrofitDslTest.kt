package com.example.william.my.core.retrofit.rx.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Retrofit
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RetrofitDslTest {

    @Test
    fun cachedRxRetrofitReusesNamedInstance() {
        val name = "rx-retrofit-dsl-reuse"

        try {
            val first = cachedRxRetrofit(name) { baseUrl("https://one.example/") }
            val second = cachedRxRetrofit(name) { baseUrl("https://two.example/") }

            assertSame(first, second)
            assertSame(first, getCachedRxRetrofit(name))
            assertEquals("one.example", first.baseUrl().host)
        } finally {
            removeCachedRxRetrofit(name)
        }
    }

    @Test
    fun cachedRxRetrofitSeparatesNamesAndSupportsRemoval() {
        val firstName = "rx-retrofit-dsl-first"
        val secondName = "rx-retrofit-dsl-second"

        try {
            val first = cachedRxRetrofit(firstName) { baseUrl("https://one.example/") }
            val second = cachedRxRetrofit(secondName) { baseUrl("https://two.example/") }

            assertNotSame(first, second)
            assertSame(first, removeCachedRxRetrofit(firstName))
            assertMissingCache(firstName)
            assertSame(second, getCachedRxRetrofit(secondName))
        } finally {
            removeCachedRxRetrofit(firstName)
            removeCachedRxRetrofit(secondName)
        }
    }

    @Test
    fun cachedRxRetrofitInitializesNamedInstanceOnceDuringConcurrentAccess() {
        val name = "rx-retrofit-dsl-concurrent"
        val initializationCount = AtomicInteger()
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(4)
        val instances = Collections.synchronizedList(mutableListOf<Retrofit>())

        try {
            val tasks = List(8) {
                executor.submit {
                    start.await()
                    instances += cachedRxRetrofit(name) {
                        initializationCount.incrementAndGet()
                        baseUrl("https://example.com/")
                    }
                }
            }
            start.countDown()
            tasks.forEach { it.get(5, TimeUnit.SECONDS) }

            assertTrue(instances.all { it === instances.first() })
            assertEquals(1, initializationCount.get())
        } finally {
            executor.shutdownNow()
            removeCachedRxRetrofit(name)
        }
    }

    @Test
    fun clearCachedRxRetrofitsAllowsNamedInstancesToBeRecreated() {
        val name = "rx-retrofit-dsl-clear"
        val first = cachedRxRetrofit(name) { baseUrl("https://one.example/") }

        try {
            clearCachedRxRetrofits()
            val second = cachedRxRetrofit(name) { baseUrl("https://two.example/") }

            assertNotSame(first, second)
            assertEquals("two.example", second.baseUrl().host)
        } finally {
            removeCachedRxRetrofit(name)
        }
    }

    private fun assertMissingCache(name: String) {
        try {
            getCachedRxRetrofit(name)
            fail("Expected NoSuchElementException")
        } catch (_: NoSuchElementException) {
            // 预期结果
        }
    }
}
