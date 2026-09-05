package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.cookie.CookieStore
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Cookie 拦截器
 *
 * 通过 [CookieStore] 加载和保存 Cookie，并保留调用方显式设置的同名 Cookie。
 */
class InterceptorCookie(private val store: CookieStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val builder: Request.Builder = request.newBuilder()

        val cookies = store.load(request.url)
        if (cookies.isNotEmpty()) {
            val callerCookieHeader = request.header("Cookie")
            val callerCookieNames = callerCookieHeader
                ?.split(';')
                ?.mapNotNull { part ->
                    part.substringBefore('=', "").trim().takeIf(String::isNotEmpty)
                }
                ?.toSet()
                .orEmpty()
            val storedCookieHeader = cookies
                .filterNot { it.name in callerCookieNames }
                .joinToString("; ") { "${it.name}=${it.value}" }
            val mergedCookieHeader = listOfNotNull(
                callerCookieHeader?.takeIf(String::isNotBlank),
                storedCookieHeader.takeIf(String::isNotBlank),
            ).joinToString("; ")
            if (mergedCookieHeader.isNotEmpty()) {
                builder.header("Cookie", mergedCookieHeader)
            }
        }

        val response: Response = chain.proceed(builder.build())

        val setCookieHeaders = response.headers("set-cookie")
        if (setCookieHeaders.isNotEmpty()) {
            val responseCookies = setCookieHeaders.mapNotNull { header ->
                Cookie.parse(request.url, header)
            }
            if (responseCookies.isNotEmpty()) {
                store.save(request.url, responseCookies)
            }
        }

        return response
    }
}
