package com.example.william.my.core.okhttp.config

import okhttp3.logging.HttpLoggingInterceptor

/**
 * 日志配置，控制 OkHttpClient 的日志输出方式。
 *
 * - [Basic]: 使用 OkHttp 官方 [HttpLoggingInterceptor]，简洁输出请求/响应摘要
 * - [None]: 不添加任何日志拦截器
 */
sealed interface LoggingConfig {

    /** 使用 OkHttp 官方 [HttpLoggingInterceptor] */
    data class Basic(
        val level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
    ) : LoggingConfig

    /** 不添加日志拦截器 */
    data object None : LoggingConfig
}
