package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.base.Header
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 请求缓存拦截器
 *
 * 根据网络状态和 [Header.RETROFIT_CACHE_ALIVE_SECOND] 设置 GET 请求的缓存策略：
 * 有网络时按指定秒数决定读取网络或缓存，无网络时仅读取缓存。
 */
class InterceptorCacheRequest(
    private val isConnected: () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        if (request.method != "GET") {
            return chain.proceed(request)
        }

        if (request.headers(Header.RETROFIT_CACHE_ALIVE_SECOND).isEmpty()) {
            return chain.proceed(request)
        }
        val age = request.headers(Header.RETROFIT_CACHE_ALIVE_SECOND)[0].toIntOrNull()
            ?: return chain.proceed(request)
        return chain.proceed(buildRequest(request, age))
    }

    /**
     * 设置由缓存还是网络请求
     */
    private fun buildRequest(request: Request, age: Int): Request {
        val builder: Request.Builder = request.newBuilder()
        return if (isConnected()) {
            if (age <= 0) {
                builder.cacheControl(CacheControl.FORCE_NETWORK).build()
            } else {
                builder
                    .cacheControl(CacheControl.Builder().maxAge(age, TimeUnit.SECONDS).build())
                    .build()
            }
        } else {
            builder.cacheControl(CacheControl.FORCE_CACHE).build()
        }
    }

}
