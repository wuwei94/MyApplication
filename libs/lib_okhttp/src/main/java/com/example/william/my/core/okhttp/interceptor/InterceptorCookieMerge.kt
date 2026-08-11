package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.cookie.internal.CallerCookieContext
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Cookie 合并拦截器
 *
 * 在网络拦截器阶段合并调用方与 [okhttp3.CookieJar] 提供的 Cookie，
 * 同名 Cookie 以调用方设置为准，跨域重定向时不转发原请求的 Cookie。
 */
internal class InterceptorCookieMerge : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val callerCookie = request.tag(CallerCookieContext::class.java)
            ?: return chain.proceed(request)
        if (request.url.host != callerCookie.host) {
            val redirectedRequest = if (request.header("Cookie") == callerCookie.value) {
                request.newBuilder().removeHeader("Cookie").build()
            } else {
                request
            }
            return chain.proceed(redirectedRequest)
        }
        val storedCookieHeader = request.header("Cookie")
            ?.split(';')
            ?.map(String::trim)
            ?.filter { part -> part.substringBefore('=').trim() !in callerCookie.names }
            ?.joinToString("; ")
            .orEmpty()
        val mergedHeader = listOf(
            callerCookie.value,
            storedCookieHeader
        ).filter(String::isNotBlank).joinToString("; ")
        val mergedRequest = request.newBuilder()
            .header("Cookie", mergedHeader)
            .build()
        return chain.proceed(mergedRequest)
    }
}
