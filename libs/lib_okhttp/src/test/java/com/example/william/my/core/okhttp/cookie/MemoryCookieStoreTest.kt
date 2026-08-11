package com.example.william.my.core.okhttp.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryCookieStoreTest {

    @Test
    fun saveMergesCookiesAndReplacesSameIdentity() {
        val store = MemoryCookieStore()
        val url = "https://example.com/account".toHttpUrl()

        store.save(url, listOf(cookie("session", "old")))
        store.save(url, listOf(cookie("theme", "dark"), cookie("session", "new")))

        val values = store.load(url).associate { it.name to it.value }
        assertEquals(mapOf("theme" to "dark", "session" to "new"), values)
    }

    @Test
    fun loadOnlyReturnsCookiesMatchingRequestPath() {
        val store = MemoryCookieStore()
        val accountUrl = "https://example.com/account/profile".toHttpUrl()
        store.save(accountUrl, listOf(cookie("account", "allowed", path = "/account")))

        assertEquals(1, store.load(accountUrl).size)
        assertTrue(store.load("https://example.com/public".toHttpUrl()).isEmpty())
    }

    @Test
    fun loadEvictsCookiesAfterTheyExpire() {
        var now = 1_000L
        val store = MemoryCookieStore { now }
        val url = "https://example.com/account".toHttpUrl()
        val expiring = Cookie.Builder()
            .name("session")
            .value("value")
            .hostOnlyDomain("example.com")
            .path("/")
            .expiresAt(2_000L)
            .build()

        store.save(url, listOf(expiring))
        assertEquals(1, store.load(url).size)

        now = 2_000L
        assertTrue(store.load(url).isEmpty())
    }

    private fun cookie(name: String, value: String, path: String = "/"): Cookie {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .hostOnlyDomain("example.com")
            .path(path)
            .build()
    }
}
