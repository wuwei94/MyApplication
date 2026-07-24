package com.example.william.my.core.volley.stack

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * OkHttp3 与 Volley 的适配器。
 *
 * 继承 [BaseHttpStack]，使 Volley 通过 OkHttp 通道发送请求，
 * 获得连接池复用、HTTP/2、拦截器等能力。
 *
 * @param client 可选，自定义的 OkHttpClient。不传则使用默认配置（含日志拦截器）。
 */
class OkHttp3Stack(
    private val client: OkHttpClient = defaultClient()
) : BaseHttpStack() {

    companion object {
        private const val TAG = "OkHttp3Stack"

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.setLevel(Level.BASIC))
            .build()
    }

    @Throws(IOException::class, AuthFailureError::class)
    override fun executeRequest(
        request: Request<*>,
        additionalHeaders: Map<String, String>
    ): HttpResponse {
        // ── URL ──
        val httpUrl = try {
            request.url.toHttpUrl()
        } catch (e: IllegalArgumentException) {
            throw IOException("Invalid URL: ${request.url}", e)
        }
        val requestBuilder = okhttp3.Request.Builder().url(httpUrl)

        // ── Headers（framework 附加 headers + request 自身 headers）──
        additionalHeaders.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }
        request.headers?.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // ── Method + Body ──
        val method = methodToString(request.method)
        val body = request.body
        if (body != null) {
            val mediaType = request.bodyContentType?.toMediaTypeOrNull()
            requestBuilder.method(method, body.toRequestBody(mediaType))
        } else {
            requestBuilder.method(method, null)
        }

        // ── 执行请求 ──
        val response = client.newCall(requestBuilder.build()).execute()

        // ── 转换为 Volley HttpResponse ──
        val responseHeaders = response.headers.map { (name, value) ->
            com.android.volley.Header(name, value)
        }
        val responseBody = response.body
        val inputStream = responseBody?.byteStream() ?: ByteArrayInputStream(byteArrayOf())
        val contentLength = responseBody?.contentLength()?.toInt() ?: 0

        return HttpResponse(response.code, responseHeaders, contentLength, inputStream)
    }

    private fun methodToString(method: Int): String = when (method) {
        Request.Method.GET -> "GET"
        Request.Method.POST -> "POST"
        Request.Method.PUT -> "PUT"
        Request.Method.DELETE -> "DELETE"
        Request.Method.HEAD -> "HEAD"
        Request.Method.PATCH -> "PATCH"
        Request.Method.OPTIONS -> "OPTIONS"
        Request.Method.TRACE -> "TRACE"
        else -> "GET"
    }
}
