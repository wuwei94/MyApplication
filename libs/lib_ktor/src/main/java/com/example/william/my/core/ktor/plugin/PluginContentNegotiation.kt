package com.example.william.my.core.ktor.plugin

import com.google.gson.Gson
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.gson.GsonConverter

/**
 * Gson 内容协商插件配置对象
 *
 * 将 Builder 生成的同一 [Gson] 实例注册到 ContentNegotiation，保证普通响应与业务信封
 * 使用一致的序列化规则。
 */
internal object PluginContentNegotiation {

    fun <T : HttpClientEngineConfig> install(
        clientConfig: HttpClientConfig<T>,
        gson: Gson,
    ) {
        clientConfig.install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter(gson))
        }
    }
}
