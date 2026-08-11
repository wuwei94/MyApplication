package com.example.william.my.core.okhttp.cookie.internal

/**
 * 调用方 Cookie 上下文
 */
internal data class CallerCookieContext(
    val value: String,
    val names: Set<String>,
    val host: String
)
