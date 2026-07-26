package com.example.william.my.core.okhttp.compat

import okhttp3.OkHttpClient

/**
 * 重试配置
 */
object CompatRetry {

    /** 设置失败重试 */
    fun setRetry(builder: OkHttpClient.Builder, retry: Boolean) {
        builder.retryOnConnectionFailure(retry)
    }
}
