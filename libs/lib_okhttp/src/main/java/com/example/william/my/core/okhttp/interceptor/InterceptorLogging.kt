package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.format.FormatParser
import com.example.william.my.core.okhttp.format.FormatParser.MAX_LOG_BODY_BYTES
import com.example.william.my.core.okhttp.format.FormatParser.isParseAble
import com.example.william.my.core.okhttp.format.FormatPrinterImpl
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 格式化日志拦截器
 *
 * 可解析的请求/响应体（JSON、XML 等）会格式化输出，
 * 不可解析或不适合安全读取的内容只输出 URL 和 Header。
 */
class InterceptorLogging(filters: List<String>) : Interceptor {

    private val mPrinter = FormatPrinterImpl(filters)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (mPrinter.shouldPrint(request.url)) {
            val requestBody = request.body
            if (isSafeToLog(requestBody)) {
                mPrinter.printJsonRequest(request, FormatParser.parseRequest(request))
            } else {
                mPrinter.printFileRequest(request)
            }
        }

        val startNs = System.nanoTime()
        val response: Response = chain.proceed(request)

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        if (mPrinter.shouldPrint(response.request.url)) {
            val responseContentType = response.body.contentType()
            if (responseContentType.isParseAble()) {
                mPrinter.printJsonResponse(
                    tookMs,
                    response,
                    responseContentType,
                    FormatParser.parseResponse(response),
                )
            } else {
                mPrinter.printFileResponse(tookMs, response)
            }
        }

        return response
    }

    /** 是否可安全读取并用于日志输出 */
    internal fun isSafeToLog(body: RequestBody?): Boolean {
        if (body == null ||
            body.isOneShot() ||
            body.isDuplex() ||
            !body.contentType().isParseAble()
        ) {
            return false
        }
        return runCatching { body.contentLength() }
            .getOrNull()
            ?.let { it in 0..MAX_LOG_BODY_BYTES }
            ?: false
    }
}
