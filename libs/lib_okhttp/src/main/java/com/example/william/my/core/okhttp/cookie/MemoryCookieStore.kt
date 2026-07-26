package com.example.william.my.core.okhttp.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * 仅内存存储的 CookieStore，应用重启后丢失。
 */
class MemoryCookieStore : CookieStore {

    private val store = mutableMapOf<String, MutableList<Cookie>>()

    override fun save(url: HttpUrl, cookies: List<Cookie>) {
        store[url.host] = cookies.toMutableList()
    }

    override fun load(url: HttpUrl): List<Cookie> {
        return store[url.host] ?: emptyList()
    }
}
