package com.example.william.my.core.okhttp.compat

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 连接池配置
 */
object CompatConnectionPool {

    /** 自定义连接池最大空闲连接数和存活时间 */
    fun setConnectionPool(
        builder: OkHttpClient.Builder,
        maxIdleConnections: Int,
        keepAliveDuration: Long,
        unit: TimeUnit = TimeUnit.MINUTES
    ) {
        builder.connectionPool(
            okhttp3.ConnectionPool(maxIdleConnections, keepAliveDuration, unit)
        )
    }
}
