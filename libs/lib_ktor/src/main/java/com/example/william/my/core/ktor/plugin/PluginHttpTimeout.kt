package com.example.william.my.core.ktor.plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.HttpTimeout

/**
 * 网络超时插件配置对象
 *
 * 将 Builder 中以秒为单位的请求、连接和 Socket 超时统一转换为 Ktor 所需的毫秒值。
 */
internal object PluginHttpTimeout {

    fun <T : HttpClientEngineConfig> install(
        clientConfig: HttpClientConfig<T>,
        requestTimeoutSeconds: Long,
        connectTimeoutSeconds: Long,
        socketTimeoutSeconds: Long,
    ) {
        clientConfig.install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutSeconds * 1000
            connectTimeoutMillis = connectTimeoutSeconds * 1000
            socketTimeoutMillis = socketTimeoutSeconds * 1000
        }
    }
}
