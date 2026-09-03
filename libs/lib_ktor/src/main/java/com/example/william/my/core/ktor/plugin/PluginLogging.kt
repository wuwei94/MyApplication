package com.example.william.my.core.ktor.plugin

import android.util.Log
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

/**
 * 安全日志插件配置对象
 *
 * 仅在日志级别不为 [LogLevel.NONE] 时安装 [Logging]，并对配置的敏感 Header 脱敏。
 */
internal object PluginLogging {

    val defaultSensitiveHeaders = setOf(
        "Authorization",
        "Proxy-Authorization",
        "Cookie",
        "Set-Cookie",
    )

    fun <T : HttpClientEngineConfig> install(
        clientConfig: HttpClientConfig<T>,
        logLevel: LogLevel,
        sanitizedHeaders: Set<String>,
    ) {
        if (logLevel == LogLevel.NONE) return

        clientConfig.install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor", message)
                }
            }
            level = logLevel
            sanitizeHeader { header ->
                sanitizedHeaders.any { it.equals(header, ignoreCase = true) }
            }
        }
    }
}
