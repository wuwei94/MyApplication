package com.example.william.my.core.ktor.plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.DefaultRequest

/**
 * 默认请求插件配置对象
 *
 * 仅在配置基础地址或公共 Header 时安装 [DefaultRequest]。
 */
internal object PluginDefaultRequest {

    fun <T : HttpClientEngineConfig> install(
        clientConfig: HttpClientConfig<T>,
        baseUrl: String?,
        defaultHeaders: Map<String, String>,
    ) {
        if (baseUrl == null && defaultHeaders.isEmpty()) return

        clientConfig.install(DefaultRequest) {
            baseUrl?.let { url(it) }
            defaultHeaders.forEach { (key, value) -> headers.append(key, value) }
        }
    }
}
