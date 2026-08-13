package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.body.DownloadProgressResponseBody
import com.example.william.my.core.okhttp.listener.ResponseProgressListener
import com.example.william.my.core.okhttp.utils.HttpLogger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 下载进度拦截器
 */
@Deprecated(
    message = "请使用 InterceptorDownloadProgress 配合 lambda 替代",
    replaceWith = ReplaceWith("InterceptorDownloadProgress")
)
class InterceptorProgress(
    private val listener: ResponseProgressListener = object : ResponseProgressListener {
        override fun onProgress(url: String, currentSize: Long, totalSize: Long) {
            HttpLogger.debug("url : " + url + " , progress : " + (+currentSize * 100 / totalSize))
        }
    }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        val body = response.body ?: return response
        return response.newBuilder()
            .body(
                DownloadProgressResponseBody(
                    request.url.toString(),
                    body,
                    listener::onProgress
                )
            )
            .build()
    }
}
