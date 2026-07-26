package com.example.william.my.core.okhttp.interceptor

import com.example.william.my.core.okhttp.format.FormatPrinterImpl
import com.example.william.my.core.okhttp.format.ParseUtils
import com.example.william.my.core.okhttp.format.ParseUtils.isParseAble
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 自定义格式化日志拦截器（边框、对齐、耗时）。
 *
 * 可解析的请求/响应体（JSON、XML 等）会格式化输出，
 * 不可解析的（文件等）只输出 URL 和 Header。
 */
class InterceptorLogging(filters: List<String>) : Interceptor {

    private val mPrinter = FormatPrinterImpl(filters)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        if (request.body?.contentType().isParseAble()) {
            mPrinter.printJsonRequest(request, ParseUtils.parseRequest(request))
        } else {
            mPrinter.printFileRequest(request)
        }

        val startNs = System.nanoTime()
        val response: Response = chain.proceed(request)

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseContentType = response.body?.contentType()
        if (responseContentType.isParseAble()) {
            mPrinter.printJsonResponse(
                tookMs,
                response,
                responseContentType,
                ParseUtils.parseResponse(response)
            )
        } else {
            mPrinter.printFileResponse(tookMs, response)
        }

        return response
    }
}
