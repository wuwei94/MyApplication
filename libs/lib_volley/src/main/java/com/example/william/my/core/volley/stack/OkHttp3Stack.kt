package com.example.william.my.core.volley.stack

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import okhttp3.HttpUrl
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
        val okRequestBuilder = okhttp3.Request.Builder()

        // ── URL + Query 参数 ──
        val urlBuilder = HttpUrl.parse(request.url)?.newBuilder()
            ?: throw IOException("Invalid URL: ${request.url}")

        if (request.method == Request.Method.GET || request.method == Request.Method.DELETE) {
            request.params?.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }
        }
        okRequestBuilder.url(urlBuilder.build())

        // ── Headers（framework 传入的附加 headers + request 自身 headers）──
        additionalHeaders.forEach { (key, value) ->
            okRequestBuilder.header(key, value)
        }
        request.headers?.forEach { (key, value) ->
            okRequestBuilder.header(key, value)
        }

        // ── Body ──
        val body = request.body
        if (body != null) {
            val content = body.body ?: byteArrayOf()
            val mediaType = body.contentType?.toMediaTypeOrNull()
            okRequestBuilder.method(request.methodName, content.toRequestBody(mediaType))
        } else {
            okRequestBuilder.method(request.methodName, null)
        }

        // ── 执行请求 ──
        val response = client.newCall(okRequestBuilder.build()).execute()

        // ── 转换为 Volley HttpResponse ──
        val responseHeaders = mutableListOf<com.android.volley.Header>()
        for ((name, value) in response.headers) {
            responseHeaders.add(com.android.volley.Header(name, value))
        }

        val responseBody = response.body
        val inputStream = responseBody?.byteStream() ?: ByteArrayInputStream(byteArrayOf())
        val contentLength = responseBody?.contentLength() ?: 0L

        return HttpResponse(
            response.code,
            responseHeaders,
            contentLength,
            inputStream
        )
    }
}
