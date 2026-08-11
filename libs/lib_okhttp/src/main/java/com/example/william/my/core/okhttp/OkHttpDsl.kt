@file:JvmName("OkHttpDsl")

package com.example.william.my.core.okhttp

import com.example.william.my.core.okhttp.builder.OkHttpClientBuilder
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * 按名称缓存的 Client 存储，线程安全。
 */
private val clientCache = ConcurrentHashMap<String, OkHttpClient>()

/**
 * 创建独立的 OkHttpClient，每次调用都新建实例。
 *
 * ```kotlin
 * val client = okHttpClient {
 *     connectTimeout(30)
 *     retryOnConnectionFailure(true)
 *     addInterceptor(myInterceptor)
 * }
 * ```
 */
fun okHttpClient(init: OkHttpClientBuilder.() -> Unit): OkHttpClient {
    return OkHttpClientBuilder().apply(init).build()
}

/**
 * 按名称缓存的 OkHttpClient，同名只创建一次，后续复用。
 *
 * ```kotlin
 * // 首次创建并缓存
 * val apiClient = cachedClient("api") {
 *     timeout(30)
 *     logging()
 * }
 *
 * // 后续调用返回同一个实例
 * val sameClient = cachedClient("api") { timeout(30) }
 * assert(apiClient === sameClient) // true
 * ```
 */
fun cachedClient(name: String, init: OkHttpClientBuilder.() -> Unit): OkHttpClient {
    return clientCache.getOrPut(name) {
        OkHttpClientBuilder().apply(init).build()
    }
}

/**
 * 获取已缓存的 Client，未缓存则抛异常。
 *
 * ```kotlin
 * val client = getCachedClient("api") // 不存在则抛 NoSuchElementException
 * ```
 */
fun getCachedClient(name: String): OkHttpClient {
    return clientCache[name]
        ?: throw NoSuchElementException("No cached client found with name: '$name'")
}

/**
 * 移除指定名称的缓存 Client。
 */
fun removeCachedClient(name: String): OkHttpClient? {
    return clientCache.remove(name)
}

/**
 * 清空所有缓存的 Client。
 */
fun clearCachedClients() {
    clientCache.clear()
}

/** 关闭由调用方拥有的 Client 资源。关闭后该 Client 不应继续使用。 */
fun OkHttpClient.closeResources() {
    runCatching { cache?.close() }
    dispatcher.executorService.shutdown()
    connectionPool.evictAll()
}

/**
 * Java 兼容：创建 OkHttpClient。
 *
 * ```java
 * OkHttpClient client = OkHttpDsl.createClient(b -> {
 *     b.timeout(30);
 *     b.retryOnConnectionFailure(true);
 * });
 * ```
 */
fun createClient(init: Consumer<OkHttpClientBuilder>): OkHttpClient {
    return OkHttpClientBuilder().apply { init.accept(this) }.build()
}

/**
 * Java 兼容：按名称缓存的 OkHttpClient。
 *
 * ```java
 * OkHttpClient client = OkHttpDsl.cachedClient("api", b -> {
 *     b.timeout(30);
 * });
 * ```
 */
fun cachedClient(name: String, init: Consumer<OkHttpClientBuilder>): OkHttpClient {
    return clientCache.getOrPut(name) {
        OkHttpClientBuilder().apply { init.accept(this) }.build()
    }
}
