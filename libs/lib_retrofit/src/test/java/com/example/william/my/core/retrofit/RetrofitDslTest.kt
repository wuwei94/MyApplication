package com.example.william.my.core.retrofit

import com.example.william.my.core.retrofit.builder.RetrofitBuilder
import com.example.william.my.core.retrofit.converter.RetrofitConverterFactory
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.lang.reflect.Type

class RetrofitDslTest {
    @Test
    fun independentRetrofitsCreateIndependentDefaultClients() {
        val first = retrofit { baseUrl("https://one.example/") }
        val second = retrofit { baseUrl("https://two.example/") }

        assertNotSame(first, second)
        assertNotSame(first.callFactory(), second.callFactory())
    }

    @Test
    fun injectedClientIsPreserved() {
        val client = OkHttpClient()
        val instance = retrofit {
            baseUrl("https://example.com/")
            client(client)
        }

        try {
            assertSame(client, instance.callFactory())
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }

    @Test
    fun cachedRetrofitReusesNamedInstance() {
        val name = "retrofit-dsl-reuse"

        try {
            val first = cachedRetrofit(name) { baseUrl("https://one.example/") }
            val second = cachedRetrofit(name) { baseUrl("https://two.example/") }

            assertSame(first, second)
            assertSame(first, getCachedRetrofit(name))
            assertTrue(first.baseUrl().host == "one.example")
        } finally {
            removeCachedRetrofit(name)
        }
    }

    @Test
    fun cachedRetrofitSeparatesNamesAndSupportsRemoval() {
        val firstName = "retrofit-dsl-first"
        val secondName = "retrofit-dsl-second"

        try {
            val first = cachedRetrofit(firstName) { baseUrl("https://one.example/") }
            val second = cachedRetrofit(secondName) { baseUrl("https://two.example/") }

            assertNotSame(first, second)
            assertSame(first, removeCachedRetrofit(firstName))
            assertMissingCache(firstName)
            assertSame(second, getCachedRetrofit(secondName))
        } finally {
            removeCachedRetrofit(firstName)
            removeCachedRetrofit(secondName)
        }
    }

    @Test
    fun cachedRetrofitInitializesNamedInstanceOnceDuringConcurrentAccess() {
        val name = "retrofit-dsl-concurrent"
        val initializationCount = AtomicInteger()
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(4)
        val instances = Collections.synchronizedList(mutableListOf<Retrofit>())

        try {
            val tasks = List(8) {
                executor.submit {
                    start.await()
                    instances += cachedRetrofit(name) {
                        initializationCount.incrementAndGet()
                        baseUrl("https://example.com/")
                    }
                }
            }
            start.countDown()
            tasks.forEach { it.get(5, TimeUnit.SECONDS) }

            assertTrue(instances.all { it === instances.first() })
            assertTrue(initializationCount.get() == 1)
        } finally {
            executor.shutdownNow()
            removeCachedRetrofit(name)
        }
    }

    @Test
    fun clearCachedRetrofitsAllowsNamedInstancesToBeRecreated() {
        val name = "retrofit-dsl-clear"
        val first = cachedRetrofit(name) { baseUrl("https://one.example/") }

        try {
            clearCachedRetrofits()
            val second = cachedRetrofit(name) { baseUrl("https://two.example/") }

            assertNotSame(first, second)
            assertTrue(second.baseUrl().host == "two.example")
        } finally {
            removeCachedRetrofit(name)
        }
    }

    @Test
    fun repeatedBuildsDoNotAccumulateDefaultConverters() {
        val builder = RetrofitBuilder()
        val first = builder.build()
        val second = builder.build()

        assertEquals(first.converterFactories().size, second.converterFactories().size)
        assertEquals(
            1,
            second.converterFactories().count { it is RetrofitConverterFactory }
        )
    }

    @Test
    fun customConverterReplacesDefaultConverter() {
        val customConverter = object : retrofit2.Converter.Factory() {}
        val builder = RetrofitBuilder().apply { converter(customConverter) }

        val instance = builder.build()
        val converters = instance.converterFactories()

        assertTrue(converters.contains(customConverter))
        assertEquals(0, converters.count { it is RetrofitConverterFactory })
    }

    @Test
    fun lastCallAdapterConfigurationReplacesPreviousOne() {
        val first = testCallAdapterFactory()
        val second = testCallAdapterFactory()
        val builder = RetrofitBuilder().apply {
            callAdapter(first)
            callAdapter(second)
        }

        val instance = builder.build()
        val adapters = instance.callAdapterFactories()

        assertFalse(adapters.contains(first))
        assertTrue(adapters.contains(second))
    }

    @Test
    fun repeatedBuildsDoNotAccumulateCallAdapters() {
        val callAdapter = testCallAdapterFactory()
        val builder = RetrofitBuilder().apply { callAdapter(callAdapter) }
        val first = builder.build()
        val second = builder.build()

        assertEquals(first.callAdapterFactories().size, second.callAdapterFactories().size)
        assertEquals(1, second.callAdapterFactories().count { it === callAdapter })
    }

    @Test
    fun defaultApiFactoryCreatesAnImplementation() {
        val api = createApi(TestApi::class.java)

        assertTrue(TestApi::class.java.isAssignableFrom(api.javaClass))
    }

    private fun assertMissingCache(name: String) {
        try {
            getCachedRetrofit(name)
            fail("Expected NoSuchElementException")
        } catch (error: NoSuchElementException) {
            assertTrue(error.message!!.contains(name))
        }
    }

    private fun testCallAdapterFactory(): CallAdapter.Factory {
        return object : CallAdapter.Factory() {
            override fun get(
                returnType: Type,
                annotations: Array<out Annotation>,
                retrofit: Retrofit
            ): CallAdapter<*, *>? = null
        }
    }

    private interface TestApi
}
