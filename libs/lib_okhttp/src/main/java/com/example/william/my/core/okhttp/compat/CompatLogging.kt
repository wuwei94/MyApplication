package com.example.william.my.core.okhttp.compat

import com.example.william.my.core.okhttp.config.LoggingConfig
import com.example.william.my.core.okhttp.interceptor.InterceptorLogging
import com.example.william.my.core.okhttp.utils.HttpLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 日志配置
 *
 * addInterceptor：在 response 被调用一次
 * addNetworkInterceptor：在 request 和 response 分别被调用一次
 *
 * 注意：当下载文件时，Level 为 Level.BODY 会发生卡死情况
 */
object CompatLogging {

    /** 根据 [LoggingConfig] 配置日志 */
    fun applyLogging(builder: OkHttpClient.Builder, config: LoggingConfig) {
        when (config) {
            is LoggingConfig.Basic -> applyBasicLog(builder, config.level)
            is LoggingConfig.None -> { /* 空操作 */ }
        }
    }

    /** 配置 OkHttp 官方日志 */
    fun applyBasicLog(
        builder: OkHttpClient.Builder,
        level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
    ) {
        builder.addInterceptor(
            HttpLoggingInterceptor { message ->
                HttpLogger.debug(message)
            }.setLevel(level)
        )
    }

    /** 配置自定义格式化日志，[filters] 为需要过滤（不打印）的 URL 后缀 */
    fun applyFormatLog(
        builder: OkHttpClient.Builder,
        filters: List<String> = emptyList()
    ) {
        builder.addInterceptor(InterceptorLogging(filters))
    }
}
