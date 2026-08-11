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

    private val builder = OkHttpClient.Builder()

    // region 超时

    /** 连接超时（秒） */
    fun connectTimeout(seconds: Long) {
        CompatTimeout.setConnectTimeout(builder, seconds)
    }

    /** 读取超时（秒） */
    fun readTimeout(seconds: Long) {
        CompatTimeout.setReadTimeout(builder, seconds)
    }

    /** 写入超时（秒） */
    fun writeTimeout(seconds: Long) {
        CompatTimeout.setWriteTimeout(builder, seconds)
    }

    /** 整体调用超时（秒） */
    fun callTimeout(seconds: Long) {
        CompatTimeout.setCallTimeout(builder, seconds)
    }

    /** 所有超时统一设置（秒） */
    fun timeout(seconds: Long) {
        CompatTimeout.setTimeout(builder, seconds)
    }

    // endregion

    // region 重试与连接池

    /** 失败重试 */
    fun retryOnConnectionFailure(retry: Boolean) {
        CompatRetry.setRetry(builder, retry)
    }

    /** 自定义连接池 */
    fun connectionPool(maxIdleConnections: Int, keepAliveDuration: Long, unit: TimeUnit = TimeUnit.MINUTES) {
        CompatConnectionPool.setConnectionPool(builder, maxIdleConnections, keepAliveDuration, unit)
    }

    // endregion

    // region 拦截器

    /** 添加应用拦截器 */
    fun addInterceptor(interceptor: Interceptor) {
        CompatInterceptor.addInterceptor(builder, interceptor)
    }

    /** 添加网络拦截器 */
    fun addNetworkInterceptor(interceptor: Interceptor) {
        CompatInterceptor.addNetworkInterceptor(builder, interceptor)
    }

    // endregion

    // region 日志

    /** 配置 OkHttp 官方日志 */
    fun logging(level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC) {
        CompatLogging.applyBasicLog(builder, level)
    }

    /** 配置自定义格式化日志，[filters] 为需要过滤（不打印）的 URL 后缀 */
    fun loggingFormat(filters: List<String> = emptyList()) {
        CompatLogging.applyFormatLog(builder, filters)
    }

    // endregion

    // region 安全

    /** 忽略 SSL 证书校验（仅调试用） */
    fun ignoreSSL() {
        CompatHttpsSSL.ignoreSSLForOkHttp(builder)
    }

    /** 禁用代理 */
    fun noProxy() {
        CompatProxy.noProxy(builder)
    }

    // endregion

    // region Cookie

    /** 启用 Cookie 管理（默认内存存储） */
    fun cookieJar() {
        CompatCookieJar.cookieJar(builder)
    }

    /** 启用 Cookie 管理（自定义存储） */
    fun cookieJar(store: CookieStore) {
        CompatCookieJar.cookieJar(builder, store)
    }

    // endregion

    // region 缓存

    /** 按目录名设置缓存 */
    fun cache(app: Application, dirName: String = "cache", dirSize: Long = 10L * 1024L * 1024L) {
        CompatCache.setCache(builder, app, dirName, dirSize)
    }

    /** 按目录设置缓存 */
    fun cache(app: Application, dir: File, dirSize: Long = 10L * 1024L * 1024L) {
        CompatCache.setCache(builder, app, dir, dirSize)
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
        builder.block()
    }

    // endregion

    internal fun build(): OkHttpClient = builder.build()
}
