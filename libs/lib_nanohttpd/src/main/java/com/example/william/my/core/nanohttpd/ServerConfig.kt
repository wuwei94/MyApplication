package com.example.william.my.core.nanohttpd

data class ServerConfig(
    val port: Int = DEFAULT_PORT,
    val timeout: Int = DEFAULT_TIMEOUT,
) {
    companion object {
        const val DEFAULT_PORT = 5567
        const val DEFAULT_TIMEOUT = 5000
    }
}
