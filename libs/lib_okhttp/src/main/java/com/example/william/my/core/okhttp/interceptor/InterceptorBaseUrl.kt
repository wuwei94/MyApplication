package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.base.Header
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 动态 BaseUrl 拦截器
 *
 * 通过在 Request Header 中添加 [Header.RETROFIT_URL_REDIRECT] 指定新的 BaseUrl，
 * 拦截器会替换请求 URL 的 scheme、host、port，并移除该 Header（不会发送到服务器）。
 *
 * ```kotlin
 * val client = okHttpClient {
 *     addInterceptor(InterceptorBaseUrl())
 * }
 *
 * // Retrofit 中使用
 * @GET("api/data")
 * @Headers("Retrofit-Url-Redirect: https://backup.example.com")
 * suspend fun getData(): Response
 *
 * // OkHttp 中使用
 * val request = Request.Builder()
 *     .url("https://api.example.com/data")
 *     .header("Retrofit-Url-Redirect", "https://backup.example.com")
 *     .build()
 * ```
 */
class InterceptorBaseUrl : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val redirectUrl = request.header(Header.RETROFIT_URL_REDIRECT)
            ?: return chain.proceed(request)

        val baseUrl = redirectUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid redirect URL: $redirectUrl")
        val newHttpUrl = request.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        val newRequest = request.newBuilder()
            .url(newHttpUrl)
            .removeHeader(Header.RETROFIT_URL_REDIRECT)
            .build()

        return chain.proceed(newRequest)
    }
}
