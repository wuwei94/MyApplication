package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.body.ProgressRequestBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 上传进度拦截器，透明监听请求体写入进度。
 *
 * 注意：OkHttp 5.x 中 [Request.Builder.body] 为 internal，
 * 因此本拦截器通过 [Request.Builder.method] 重新设置 body 来实现进度包装。
 *
 * ```kotlin
 * val client = okHttpClient {
 *     addNetworkInterceptor(InterceptorUploadProgress { current, total ->
 *         Log.d("Upload", "$current/$total")
 *     })
 * }
 * ```
 */
class InterceptorUploadProgress(
    private val listener: (currentBytes: Long, totalBytes: Long) -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body ?: return chain.proceed(request)
        val wrappedBody = ProgressRequestBody(requestBody, listener)
        val newRequest = request.newBuilder()
            .method(request.method, wrappedBody)
            .build()
        return chain.proceed(newRequest)
    }
}
