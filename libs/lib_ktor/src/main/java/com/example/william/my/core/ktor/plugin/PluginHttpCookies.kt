package com.example.william.my.core.ktor.plugin

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

/**
 * Cookie 管理插件配置对象
 *
 * 仅在调用方提供 [CookiesStorage] 时安装 [HttpCookies]。
 */
internal object PluginHttpCookies {

    fun <T : HttpClientEngineConfig> install(
        clientConfig: HttpClientConfig<T>,
        storage: CookiesStorage?,
    ) {
        storage ?: return
        clientConfig.install(HttpCookies) {
            this.storage = storage
        }
    }
}
