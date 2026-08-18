package com.example.william.my.core.okhttp.builder

import android.app.Application
import com.example.william.my.core.okhttp.compat.CompatCache
import com.example.william.my.core.okhttp.compat.CompatConnectionPool
import com.example.william.my.core.okhttp.compat.CompatCookieJar
import com.example.william.my.core.okhttp.compat.CompatHttpsSSL
import com.example.william.my.core.okhttp.compat.CompatInterceptor
import com.example.william.my.core.okhttp.compat.CompatLogging
import com.example.william.my.core.okhttp.compat.CompatProxy
import com.example.william.my.core.okhttp.compat.CompatRetry
import com.example.william.my.core.okhttp.compat.CompatTimeout
import com.example.william.my.core.okhttp.cookie.CookieStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * OkHttp DSL 作用域标记
 */
@DslMarker
annotation class OkHttpDslMarker

/**
 * OkHttpClient DSL 构建器
 */
@OkHttpDslMarker
class OkHttpClientBuilder {

    private var connectTimeoutSeconds: Long? = null
    private var readTimeoutSeconds: Long? = null
    private var writeTimeoutSeconds: Long? = null
    private var callTimeoutSeconds: Long? = null
    private var retryOnConnectionFailure: Boolean? = null
    private var connectionPoolConfig: ConnectionPoolConfig? = null
    private val interceptors = mutableListOf<Interceptor>()
    private val networkInterceptors = mutableListOf<Interceptor>()
    private var basicLogLevel: HttpLoggingInterceptor.Level? = null
    private var formatLogFilters: List<String>? = null
    private var ignoreSsl = false
    private var noProxy = false
    private var cookieJarEnabled = false
    private var cookieStore: CookieStore? = null
    private var cacheAppDirConfig: CacheAppDirConfig? = null
    private var cacheAppFileConfig: CacheAppFileConfig? = null
    private val rawBlocks = mutableListOf<OkHttpClient.Builder.() -> Unit>()

    // region 超时

    /** 连接超时（秒） */
    fun connectTimeout(seconds: Long) {
        connectTimeoutSeconds = seconds
    }

    /** 读取超时（秒） */
    fun readTimeout(seconds: Long) {
        readTimeoutSeconds = seconds
    }

    /** 写入超时（秒） */
    fun writeTimeout(seconds: Long) {
        writeTimeoutSeconds = seconds
    }

    /** 整体调用超时（秒） */
    fun callTimeout(seconds: Long) {
        callTimeoutSeconds = seconds
    }

    /** 所有超时统一设置（秒） */
    fun timeout(seconds: Long) {
        connectTimeoutSeconds = seconds
        readTimeoutSeconds = seconds
        writeTimeoutSeconds = seconds
        callTimeoutSeconds = seconds
    }

    // endregion

    // region 重试与连接池

    /** 失败重试 */
    fun retryOnConnectionFailure(retry: Boolean) {
        retryOnConnectionFailure = retry
    }

    /** 自定义连接池 */
    fun connectionPool(maxIdleConnections: Int, keepAliveDuration: Long, unit: TimeUnit = TimeUnit.MINUTES) {
        connectionPoolConfig = ConnectionPoolConfig(maxIdleConnections, keepAliveDuration, unit)
    }

    // endregion

    // region 拦截器

    /** 添加应用拦截器 */
    fun addInterceptor(interceptor: Interceptor) {
        interceptors += interceptor
    }

    /** 添加网络拦截器 */
    fun addNetworkInterceptor(interceptor: Interceptor) {
        networkInterceptors += interceptor
    }

    // endregion

    // region 日志

    /** 配置 OkHttp 官方日志 */
    fun logging(level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC) {
        basicLogLevel = level
    }

    /** 配置自定义格式化日志，[filters] 为需要过滤（不打印）的 URL 后缀 */
    fun loggingFormat(filters: List<String> = emptyList()) {
        formatLogFilters = filters
    }

    // endregion

    // region 安全

    /** 忽略 SSL 证书校验（仅调试用） */
    fun ignoreSSL() {
        ignoreSsl = true
    }

    /** 禁用代理 */
    fun noProxy() {
        noProxy = true
    }

    // endregion

    // region Cookie

    /** 启用 Cookie 管理（默认内存存储） */
    fun cookieJar() {
        cookieJarEnabled = true
        cookieStore = null
    }

    /** 启用 Cookie 管理（自定义存储） */
    fun cookieJar(store: CookieStore) {
        cookieJarEnabled = true
        cookieStore = store
    }

    // endregion

    // region 缓存

    /** 按目录名设置缓存 */
    fun cache(app: Application, dirName: String = "cache", dirSize: Long = 10L * 1024L * 1024L) {
        cacheAppDirConfig = CacheAppDirConfig(app, dirName, dirSize)
        cacheAppFileConfig = null
    }

    /** 按目录设置缓存 */
    fun cache(app: Application, dir: File, dirSize: Long = 10L * 1024L * 1024L) {
        cacheAppFileConfig = CacheAppFileConfig(app, dir, dirSize)
        cacheAppDirConfig = null
    }

    // endregion

    // region 高级配置

    /**
     * 直接操作底层 OkHttpClient.Builder。
     *
     * **安全提示**：调用方在 [block] 中自行配置 SSL 时需确保
     * 不会在 Release 构建中意外禁用证书校验。
     */
    fun raw(block: OkHttpClient.Builder.() -> Unit) {
        rawBlocks += block
    }

    // endregion

    internal fun build(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        connectTimeoutSeconds?.let { CompatTimeout.setConnectTimeout(builder, it) }
        readTimeoutSeconds?.let { CompatTimeout.setReadTimeout(builder, it) }
        writeTimeoutSeconds?.let { CompatTimeout.setWriteTimeout(builder, it) }
        callTimeoutSeconds?.let { CompatTimeout.setCallTimeout(builder, it) }

        retryOnConnectionFailure?.let { CompatRetry.setRetry(builder, it) }
        connectionPoolConfig?.let {
            CompatConnectionPool.setConnectionPool(builder, it.maxIdleConnections, it.keepAliveDuration, it.unit)
        }

        if (ignoreSsl) {
            CompatHttpsSSL.ignoreSSLForOkHttp(builder)
        }

        if (noProxy) {
            CompatProxy.noProxy(builder)
        }

        if (cookieJarEnabled) {
            cookieStore?.let { CompatCookieJar.cookieJar(builder, it) } ?: CompatCookieJar.cookieJar(builder)
        }

        cacheAppDirConfig?.let {
            CompatCache.setCache(builder, it.app, it.dirName, it.dirSize)
        }
        cacheAppFileConfig?.let {
            CompatCache.setCache(builder, it.app, it.dir, it.dirSize)
        }

        interceptors.forEach { builder.addInterceptor(it) }
        networkInterceptors.forEach { builder.addNetworkInterceptor(it) }

        basicLogLevel?.let { CompatLogging.applyBasicLog(builder, it) }
        formatLogFilters?.let { CompatLogging.applyFormatLog(builder, it) }

        rawBlocks.forEach { block -> builder.block() }

        return builder.build()
    }

    private data class ConnectionPoolConfig(
        val maxIdleConnections: Int,
        val keepAliveDuration: Long,
        val unit: TimeUnit,
    )

    private data class CacheAppDirConfig(
        val app: Application,
        val dirName: String,
        val dirSize: Long,
    )

    private data class CacheAppFileConfig(
        val app: Application,
        val dir: File,
        val dirSize: Long,
    )
}
