package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.cookie.internal.CallerCookieContext
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Cookie 捕获拦截器
 *
 * 在应用拦截器阶段记录调用方显式设置的 Cookie，供网络拦截器阶段合并。
 */
internal class InterceptorCookieCapture : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val callerHeader = request.header("Cookie")?.takeIf(String::isNotBlank)
            ?: return chain.proceed(request)
        val taggedRequest = request.newBuilder()
            .tag(
                CallerCookieContext::class.java,
                CallerCookieContext(callerHeader, cookieNames(callerHeader), request.url.host)
            )
            .build()
        return chain.proceed(taggedRequest)
    }

    private fun cookieNames(header: String): Set<String> {
        return header.split(';').mapNotNull { part ->
            part.substringBefore('=', "").trim().takeIf(String::isNotEmpty)
        }.toSet()
    }
}
