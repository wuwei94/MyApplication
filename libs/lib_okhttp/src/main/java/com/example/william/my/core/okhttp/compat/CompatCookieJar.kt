package com.example.william.my.core.okhttp.compat

import com.example.william.my.core.okhttp.cookie.CookieStore
import com.example.william.my.core.okhttp.cookie.MemoryCookieStore
import com.example.william.my.core.okhttp.interceptor.InterceptorCookie
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

object CompatCookieJar {

    /**
     * 通过 OkHttp 的 [CookieJar] 设置 Cookie 管理（默认内存存储）。
     */
    fun cookieJar(builder: OkHttpClient.Builder) {
        cookieJar(builder, MemoryCookieStore())
    }

    /**
     * 通过 OkHttp 的 [CookieJar] 设置 Cookie 管理，支持自定义 [CookieStore]。
     */
    fun cookieJar(builder: OkHttpClient.Builder, store: CookieStore) {
        builder.cookieJar(OkHttpCookieJarAdapter(store))
    }

    /**
     * 通过拦截器设置 Cookie 管理（默认内存存储）。
     */
    @Deprecated(
        message = "请使用 cookieJar() 方式替代",
        replaceWith = ReplaceWith("cookieJar()")
    )
    fun cookieJarByInterceptor(builder: OkHttpClient.Builder) {
        cookieJarByInterceptor(builder, MemoryCookieStore())
    }

    /**
     * 通过拦截器设置 Cookie 管理，支持自定义 [CookieStore]。
     */
    @Deprecated(
        message = "请使用 cookieJar(store) 方式替代",
        replaceWith = ReplaceWith("cookieJar(store)")
    )
    fun cookieJarByInterceptor(builder: OkHttpClient.Builder, store: CookieStore) {
        builder.addInterceptor(InterceptorCookie(store))
    }
}

private class OkHttpCookieJarAdapter(
    private val store: CookieStore
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store.save(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store.load(url)
    }
}
