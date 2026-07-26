package com.example.william.my.core.okhttp.compat

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 超时配置
 */
object CompatTimeout {

    /** 设置连接超时（秒） */
    fun setConnectTimeout(builder: OkHttpClient.Builder, seconds: Long) {
        builder.connectTimeout(seconds, TimeUnit.SECONDS)
    }

    /** 设置写入超时（秒） */
    fun setWriteTimeout(builder: OkHttpClient.Builder, seconds: Long) {
        builder.writeTimeout(seconds, TimeUnit.SECONDS)
    }

    /** 设置读取超时（秒） */
    fun setReadTimeout(builder: OkHttpClient.Builder, seconds: Long) {
        builder.readTimeout(seconds, TimeUnit.SECONDS)
    }

    /** 设置整体调用超时（秒） */
    fun setCallTimeout(builder: OkHttpClient.Builder, seconds: Long) {
        builder.callTimeout(seconds, TimeUnit.SECONDS)
    }

    /** 统一设置所有超时（秒） */
    fun setTimeout(builder: OkHttpClient.Builder, seconds: Long) {
        builder.connectTimeout(seconds, TimeUnit.SECONDS)
        builder.writeTimeout(seconds, TimeUnit.SECONDS)
        builder.readTimeout(seconds, TimeUnit.SECONDS)
        builder.callTimeout(seconds, TimeUnit.SECONDS)
    }
}
