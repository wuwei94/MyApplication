@file:JvmName("RetrofitDsl")

package com.example.william.my.core.retrofit

import com.example.william.my.core.retrofit.builder.RetrofitBuilder
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * 按名称缓存的 Retrofit 存储，线程安全。
 */
private val retrofitCache = ConcurrentHashMap<String, Retrofit>()

/**
 * 创建独立的 Retrofit 实例，每次调用都新建。
 *
 * ```kotlin
 * val retrofit = retrofit {
 *     baseUrl("https://api.example.com/")
 *     client(okHttpClient { logging() })
 * }
 * ```
 */
fun retrofit(init: RetrofitBuilder.() -> Unit): Retrofit {
    return RetrofitBuilder().apply(init).build()
}

/**
 * 按名称缓存的 Retrofit 实例，同名只创建一次，后续复用。
 *
 * ```kotlin
 * val api = cachedRetrofit("api") {
 *     baseUrl("https://api.example.com/")
 *     client(okHttpClient { timeout(30); logging() })
 * }
 * val same = cachedRetrofit("api") { baseUrl("...") }
 * assert(api === same) // true
 * ```
 */
fun cachedRetrofit(name: String, init: RetrofitBuilder.() -> Unit): Retrofit {
    return retrofitCache.getOrPut(name) {
        RetrofitBuilder().apply(init).build()
    }
}

/**
 * 获取已缓存的 Retrofit 实例，未缓存则抛异常。
 */
fun getCachedRetrofit(name: String): Retrofit {
    return retrofitCache[name]
        ?: throw NoSuchElementException("No cached retrofit found with name: '$name'")
}

/**
 * 移除指定名称的缓存 Retrofit 实例。
 */
fun removeCachedRetrofit(name: String): Retrofit? {
    return retrofitCache.remove(name)
}

/**
 * 清空所有缓存的 Retrofit 实例。
 */
fun clearCachedRetrofits() {
    retrofitCache.clear()
}

/**
 * Java 兼容：创建 Retrofit 实例。
 *
 * ```java
 * Retrofit r = RetrofitDsl.createRetrofit(b -> {
 *     b.baseUrl("https://api.example.com/");
 *     b.client(client);
 * });
 * ```
 */
fun createRetrofit(init: Consumer<RetrofitBuilder>): Retrofit {
    return RetrofitBuilder().apply { init.accept(this) }.build()
}

/**
 * Java 兼容：按名称缓存的 Retrofit 实例。
 *
 * ```java
 * Retrofit r = RetrofitDsl.cachedRetrofit("api", b -> {
 *     b.baseUrl("https://api.example.com/");
 * });
 * ```
 */
fun cachedRetrofit(name: String, init: Consumer<RetrofitBuilder>): Retrofit {
    return retrofitCache.getOrPut(name) {
        RetrofitBuilder().apply { init.accept(this) }.build()
    }
}
