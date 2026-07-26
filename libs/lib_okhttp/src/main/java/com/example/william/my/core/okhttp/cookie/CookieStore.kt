package com.example.william.my.core.okhttp.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Cookie 存储接口，支持自定义持久化策略。
 */
interface CookieStore {
    fun save(url: HttpUrl, cookies: List<Cookie>)
    fun load(url: HttpUrl): List<Cookie>
}
