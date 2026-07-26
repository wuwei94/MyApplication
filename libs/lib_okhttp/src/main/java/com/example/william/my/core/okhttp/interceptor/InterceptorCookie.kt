package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.cookie.CookieStore
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Cookie拦截器，通过 [CookieStore] 实现可插拔存储。
 */
class InterceptorCookie(private val store: CookieStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val builder: Request.Builder = request.newBuilder()

        val cookies = store.load(request.url)
        if (cookies.isNotEmpty()) {
            val cookieHeader = cookies.joinToString("; ") { "${it.name}=${it.value}" }
            builder.addHeader("Cookie", cookieHeader)
        }

        val response: Response = chain.proceed(builder.build())

        val setCookies = response.headers("set-cookie")
        if (setCookies.isNotEmpty()) {
            store.save(request.url, cookies)
        }

        return response
    }
}
