package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.base.Header
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 响应缓存拦截器
 *
 * 请求发送前移除内部缓存 Header，并根据其秒数更新响应的 Cache-Control。
 */
internal class InterceptorCacheResponse : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val cacheAgeHeader = request.header(Header.RETROFIT_CACHE_ALIVE_SECOND)
            ?: return chain.proceed(request)
        val networkRequest = request.newBuilder()
            .removeHeader(Header.RETROFIT_CACHE_ALIVE_SECOND)
            .build()
        val response = chain.proceed(networkRequest)
        val age = cacheAgeHeader.toIntOrNull()
        return if (request.method == "GET" && age != null) {
            buildResponse(response, age)
        } else {
            response
        }
    }

    private fun buildResponse(response: Response, age: Int): Response {
        val builder = response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
        return if (age <= 0) {
            builder.build()
        } else {
            builder.header("Cache-Control", "public, max-age=$age").build()
        }
    }
}
