package com.example.william.my.core.okhttp.cookie.internal

import com.example.william.my.core.okhttp.cookie.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * CookieJar 适配器
 */
internal class OkHttpCookieJarAdapter(
    private val store: CookieStore,
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store.save(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = store.load(url)
}
