package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.body.ProgressResponseBody
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 下载进度拦截器，透明监听响应体读取进度。
 *
 * ```kotlin
 * val client = okHttpClient {
 *     addInterceptor(InterceptorDownloadProgress { url, current, total ->
 *         Log.d("Progress", "$url: $current/$total")
 *     })
 * }
 * ```
 */
class InterceptorDownloadProgress(
    private val listener: (url: String, currentBytes: Long, totalBytes: Long) -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val body = response.body ?: return response

        return response.newBuilder()
            .body(ProgressResponseBody(request.url.toString(), body, listener))
            .build()
    }
}
