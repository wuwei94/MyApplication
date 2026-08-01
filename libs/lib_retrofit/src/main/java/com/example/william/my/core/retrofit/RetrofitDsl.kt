@file:JvmName("RetrofitDsl")

package com.example.william.my.core.retrofit

import com.example.william.my.core.retrofit.builder.RetrofitBuilder
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * 无依赖注入场景使用的按名称 Retrofit 缓存。
 *
 * 正式业务应优先由 Hilt 或 ServiceLocator 同时管理 OkHttpClient、Retrofit 和 API Service，
 * 避免同一组实例混用两套生命周期管理方式。
 */
private val retrofitCache = ConcurrentHashMap<String, Retrofit>()

private val defaultRetrofit: Retrofit by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    RetrofitBuilder().build()
}

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
 * 按名称缓存 Retrofit，同名实例只会完成一次初始化并在后续调用中复用。
 *
 * 该入口适用于未使用 Hilt 或 ServiceLocator 的简单场景。若实例已由应用层容器管理，
 * 请使用 [retrofit] 创建并由容器持有，不要再注册到此缓存。
 */
fun cachedRetrofit(name: String, init: RetrofitBuilder.() -> Unit): Retrofit {
    retrofitCache[name]?.let { return it }
    return synchronized(retrofitCache) {
        retrofitCache[name] ?: RetrofitBuilder().apply(init).build().also {
            retrofitCache[name] = it
        }
    }
}

/** 获取已缓存的 Retrofit；指定名称不存在时抛出 [NoSuchElementException]。 */
fun getCachedRetrofit(name: String): Retrofit {
    return retrofitCache[name]
        ?: throw NoSuchElementException("No cached retrofit found with name: '$name'")
}

/** 移除并返回指定名称的 Retrofit；不存在时返回 `null`。 */
fun removeCachedRetrofit(name: String): Retrofit? {
    return synchronized(retrofitCache) {
        retrofitCache.remove(name)
    }
}

/** 清空通过 [cachedRetrofit] 创建的所有 Retrofit，不影响内部默认实例。 */
fun clearCachedRetrofits() {
    synchronized(retrofitCache) {
        retrofitCache.clear()
    }
}

/**
 * 创建 API 接口实例，默认复用库内的 Retrofit。
 *
 * ```kotlin
 * val api = createApi(NetworkApi::class.java)
 * val api2 = createApi(NetworkApi::class.java, myRetrofit)
 * ```
 */
fun <T> createApi(api: Class<T>, retrofit: Retrofit = defaultRetrofit): T {
    return retrofit.create(api)
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
 * Java 兼容：按名称缓存 Retrofit。
 *
 * 该入口仅用于未使用 Hilt 或 ServiceLocator 的简单场景。
 */
fun cachedRetrofit(name: String, init: Consumer<RetrofitBuilder>): Retrofit {
    return cachedRetrofit(name) { init.accept(this) }
}
