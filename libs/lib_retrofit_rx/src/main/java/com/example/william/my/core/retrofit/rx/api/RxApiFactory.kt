@file:JvmName("RxApiFactory")

package com.example.william.my.core.retrofit.rx.api

import com.example.william.my.core.retrofit.builder.RetrofitBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/** 无依赖注入场景使用的按名称 Rx Retrofit 缓存。 */
private val rxRetrofitCache = ConcurrentHashMap<String, Retrofit>()

private val defaultRxRetrofit: Retrofit by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    rxRetrofit()
}

/** 创建已安装 RxJava3 CallAdapter 的 Retrofit 实例。 */
fun rxRetrofit(init: RetrofitBuilder.() -> Unit = {}): Retrofit {
    return com.example.william.my.core.retrofit.retrofit {
        init()
        callAdapter(RxJava3CallAdapterFactory.create())
    }
}

/**
 * 按名称缓存已安装 RxJava3 CallAdapter 的 Retrofit，同名实例只初始化一次。
 *
 * 该入口适用于未使用 Hilt 或 ServiceLocator 的简单场景。由应用层容器管理的实例
 * 不应同时放入此缓存，以免出现两套生命周期。
 */
fun cachedRxRetrofit(
    name: String,
    init: RetrofitBuilder.() -> Unit,
): Retrofit {
    rxRetrofitCache[name]?.let { return it }
    return synchronized(rxRetrofitCache) {
        rxRetrofitCache[name] ?: rxRetrofit(init).also {
            rxRetrofitCache[name] = it
        }
    }
}

/** 获取已缓存的 Rx Retrofit；指定名称不存在时抛出 [NoSuchElementException]。 */
fun getCachedRxRetrofit(name: String): Retrofit {
    return rxRetrofitCache[name]
        ?: throw NoSuchElementException("No cached Rx Retrofit found with name: '$name'")
}

/** 移除并返回指定名称的 Rx Retrofit；不存在时返回 `null`。 */
fun removeCachedRxRetrofit(name: String): Retrofit? {
    return synchronized(rxRetrofitCache) {
        rxRetrofitCache.remove(name)
    }
}

/** 清空通过 [cachedRxRetrofit] 创建的所有实例，不影响内部默认实例。 */
fun clearCachedRxRetrofits() {
    synchronized(rxRetrofitCache) {
        rxRetrofitCache.clear()
    }
}

/** 创建基于默认启用 Rx 的 Retrofit 实例的 API。 */
fun <T> createRxApi(
    api: Class<T>,
    retrofit: Retrofit = defaultRxRetrofit
): T = retrofit.create(api)

/** Java 兼容：按名称缓存已安装 RxJava3 CallAdapter 的 Retrofit。 */
fun cachedRxRetrofit(
    name: String,
    init: Consumer<RetrofitBuilder>,
): Retrofit {
    return cachedRxRetrofit(name) { init.accept(this) }
}
