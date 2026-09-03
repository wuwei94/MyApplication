package com.example.william.my.core.ktor.builder

import android.app.Application
import com.example.william.my.core.ktor.client.KtorClient
import com.example.william.my.core.ktor.converter.DEFAULT_CODE_FIELD
import com.example.william.my.core.ktor.converter.DEFAULT_MESSAGE_FIELD
import com.example.william.my.core.ktor.converter.KtorResponseCodeFieldKey
import com.example.william.my.core.ktor.converter.KtorResponseGsonKey
import com.example.william.my.core.ktor.converter.KtorResponseMessageFieldKey
import com.example.william.my.core.ktor.plugin.PluginContentNegotiation
import com.example.william.my.core.ktor.plugin.PluginDefaultRequest
import com.example.william.my.core.ktor.plugin.PluginHttpCookies
import com.example.william.my.core.ktor.plugin.PluginHttpTimeout
import com.example.william.my.core.ktor.plugin.PluginLogging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.logging.LogLevel
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI

/**
 * Ktor Client DSL 作用域标记
 */
@DslMarker
annotation class KtorDslMarker

/**
 * Ktor Client DSL 构建器
 *
 * 负责收集项目级网络配置、安装内置 Plugin，并构建固定使用 OkHttp Engine 的独立 Client。
 */
@KtorDslMarker
class KtorClientBuilder {

    private var preconfiguredOkHttpClient: OkHttpClient? = null
    private var baseUrl: String? = null
    private var enableRedirects = true
    private var requestTimeoutSeconds = 15L
    private var connectTimeoutSeconds = 15L
    private var socketTimeoutSeconds = 15L
    private var retryOnConnectionFailure = true
    private var cookieStorage: CookiesStorage? = null
    private var diskCache: Cache? = null
    private var logLevel = LogLevel.NONE
    private var sanitizedHeaders = PluginLogging.defaultSensitiveHeaders
    private val defaultHeaders = mutableMapOf<String, String>()
    private var codeField = DEFAULT_CODE_FIELD
    private var messageField = DEFAULT_MESSAGE_FIELD
    private var gsonConfig: GsonBuilder.() -> Unit = { serializeNulls() }
    private val clientConfigurations = mutableListOf<HttpClientConfig<*>.() -> Unit>()

    /**
     * 使用应用自有的 OkHttpClient 作为 Ktor OkHttp 引擎的基础配置。
     * Ktor 通过 [OkHttpClient.newBuilder] 派生实际客户端，替换其 Dispatcher，并应用
     * Ktor 的超时/重定向设置。关闭 Ktor 不会关闭注入的实例，但会驱逐派生客户端使用的共享连接池。
     */
    fun client(okHttpClient: OkHttpClient) {
        preconfiguredOkHttpClient = okHttpClient
    }

    /** 设置基础地址，必须是以 `/` 结尾的 HTTP(S) 绝对地址。 */
    fun baseUrl(url: String) {
        val parsed = runCatching { URI(url) }.getOrNull()
        require(
            parsed?.isAbsolute == true &&
                parsed.host != null &&
                (
                    parsed.scheme.equals("http", ignoreCase = true) ||
                        parsed.scheme.equals("https", ignoreCase = true)
                    ),
        ) {
            "baseUrl must be an absolute http or https URL"
        }
        require(parsed.rawQuery == null && parsed.rawFragment == null) {
            "baseUrl must not contain query or fragment"
        }
        require(parsed.path.endsWith('/')) { "baseUrl path must end with '/'" }
        baseUrl = url
    }

    fun followRedirects(enabled: Boolean = true) {
        enableRedirects = enabled
    }

    fun timeout(seconds: Long) {
        require(seconds > 0) { "timeout must be greater than zero" }
        requestTimeoutSeconds = seconds
        connectTimeoutSeconds = seconds
        socketTimeoutSeconds = seconds
    }

    /** 整体请求超时（秒）。 */
    fun requestTimeout(seconds: Long) {
        requestTimeoutSeconds = requirePositiveTimeout(seconds)
    }

    /** 建立连接超时（秒）。 */
    fun connectTimeout(seconds: Long) {
        connectTimeoutSeconds = requirePositiveTimeout(seconds)
    }

    /** 相邻数据包读写间隔超时（秒）。 */
    fun socketTimeout(seconds: Long) {
        socketTimeoutSeconds = requirePositiveTimeout(seconds)
    }

    /** 设置连接失败重试，与 OkHttpClient.Builder 的同名配置保持一致。 */
    fun retryOnConnectionFailure(retry: Boolean) {
        retryOnConnectionFailure = retry
    }

    /** 启用 Cookie 管理，默认使用内存存储。 */
    fun cookies(storage: CookiesStorage = AcceptAllCookiesStorage()) {
        cookieStorage = storage
    }

    /** 使用应用缓存目录启用 OkHttp 磁盘缓存。 */
    fun cache(
        app: Application,
        dirName: String = "ktor-cache",
        maxSize: Long = DEFAULT_CACHE_SIZE,
    ) {
        cache(File(app.cacheDir, dirName), maxSize)
    }

    /** 使用指定目录启用 OkHttp 磁盘缓存。 */
    fun cache(directory: File, maxSize: Long = DEFAULT_CACHE_SIZE) {
        require(maxSize > 0) { "cache maxSize must be greater than zero" }
        diskCache = Cache(directory, maxSize)
    }

    /**
     * 启用 Ktor 日志，默认只记录 Header，并对认证及 Cookie Header 脱敏。
     */
    fun logging(
        level: LogLevel = LogLevel.HEADERS,
        sensitiveHeaders: Set<String> = PluginLogging.defaultSensitiveHeaders,
    ) {
        logLevel = level
        sanitizedHeaders = sensitiveHeaders
    }

    fun header(key: String, value: String) {
        defaultHeaders[key] = value
    }

    /** 设置响应码字段名，与 RetrofitBuilder 的同名配置保持一致。 */
    fun code(key: String) {
        codeField = key
    }

    /** 设置响应消息字段名，与 RetrofitBuilder 的同名配置保持一致。 */
    fun message(key: String) {
        messageField = key
    }

    /** 同时配置普通响应与业务信封解析使用的 Gson。 */
    fun gson(block: GsonBuilder.() -> Unit) {
        gsonConfig = {
            serializeNulls()
            block()
        }
    }

    /** 直接操作底层 HttpClientConfig，配置在内置默认项之后执行。 */
    fun raw(block: HttpClientConfig<*>.() -> Unit) {
        clientConfigurations += block
    }

    /** 构建固定使用 OkHttp Engine 的 Ktor Client。 */
    internal fun build(): KtorClient {
        val gson = GsonBuilder().apply(gsonConfig).create()
        return HttpClient(OkHttp) {
            configureCommon(gson)
            engine {
                preconfigured = preconfiguredOkHttpClient
                config {
                    // 重定向必须经由 Ktor 的重定向插件处理，以确保认证头被剥离。
                    followRedirects(false)
                    followSslRedirects(false)
                    retryOnConnectionFailure(retryOnConnectionFailure)
                    diskCache?.let(::cache)
                }
            }
        }.also { client ->
            client.attributes.put(KtorResponseCodeFieldKey, codeField)
            client.attributes.put(KtorResponseMessageFieldKey, messageField)
            client.attributes.put(KtorResponseGsonKey, gson)
        }
    }

    private fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureCommon(gson: Gson) {
        expectSuccess = true
        followRedirects = enableRedirects

        PluginContentNegotiation.install(this, gson)
        PluginHttpTimeout.install(
            clientConfig = this,
            requestTimeoutSeconds = requestTimeoutSeconds,
            connectTimeoutSeconds = connectTimeoutSeconds,
            socketTimeoutSeconds = socketTimeoutSeconds,
        )
        PluginHttpCookies.install(this, cookieStorage)
        PluginDefaultRequest.install(this, baseUrl, defaultHeaders)
        PluginLogging.install(this, logLevel, sanitizedHeaders)
        clientConfigurations.forEach { configure -> configure(this) }
    }

    private fun requirePositiveTimeout(seconds: Long): Long {
        require(seconds > 0) { "timeout must be greater than zero" }
        return seconds
    }

    companion object {
        private const val DEFAULT_CACHE_SIZE = 10L * 1024L * 1024L
    }
}
