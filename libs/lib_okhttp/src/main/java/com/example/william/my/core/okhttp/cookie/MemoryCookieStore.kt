package com.example.william.my.core.okhttp.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * 内存 Cookie 存储，线程安全，应用重启后丢失。
 *
 * Cookie 按 name/domain/path 合并，并在加载时过滤过期及不匹配当前 URL 的条目。
 */
class MemoryCookieStore() : CookieStore {

    private val lock = Any()
    private val store = mutableListOf<Cookie>()
    private var nowMillis: () -> Long = System::currentTimeMillis

    internal constructor(nowMillis: () -> Long) : this() {
        this.nowMillis = nowMillis
    }

    override fun save(url: HttpUrl, cookies: List<Cookie>) {
        val now = nowMillis()
        synchronized(lock) {
            store.removeAll { stored ->
                stored.expiresAt <= now || cookies.any { incoming -> incoming.sameIdentityAs(stored) }
            }
            store += cookies.filter { it.expiresAt > now }
        }
    }

    override fun clear() {
        synchronized(lock) { store.clear() }
    }

    override fun load(url: HttpUrl): List<Cookie> {
        val now = nowMillis()
        return synchronized(lock) {
            store.removeAll { it.expiresAt <= now }
            store.filter { it.matches(url) }
        }
    }

    private fun Cookie.sameIdentityAs(other: Cookie): Boolean {
        return name == other.name && domain == other.domain && path == other.path
    }
}
