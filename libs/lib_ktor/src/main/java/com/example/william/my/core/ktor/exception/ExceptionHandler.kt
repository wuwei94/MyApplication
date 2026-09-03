package com.example.william.my.core.ktor.exception

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 网络异常转换器
 *
 * 将 Ktor、网络、SSL 和 JSON 异常转换为统一的 [ApiException]，协程取消继续向上传播。
 */
object ExceptionHandler {

    suspend fun handleException(e: Throwable): ApiException {
        if (e is CancellationException) throw e
        return when (e) {
            is ResponseException -> {
                val status = e.response.status.value
                val body = extractErrorBody(e)
                ApiException(e, status).apply {
                    message = extractErrorMessage(body) ?: "请求错误($status)"
                }
            }
            is HttpRequestTimeoutException, is SocketTimeoutException -> {
                ApiException(e, ApiException.Error.TIMEOUT_ERROR).apply {
                    message = "请求超时，请稍后再试"
                }
            }
            is ConnectException, is UnknownHostException -> {
                ApiException(e, ApiException.Error.CONNECT_ERROR).apply {
                    message = "连接失败，请检查网络设置"
                }
            }
            is SSLException -> {
                ApiException(e, ApiException.Error.SSL_ERROR).apply {
                    message = "证书校验失败，请稍后再试"
                }
            }
            is JsonConvertException, is JsonParseException, is JSONException -> {
                ApiException(e, ApiException.Error.PARSE_ERROR).apply {
                    message = "解析错误，请稍后再试"
                }
            }
            else -> {
                ApiException(e, ApiException.Error.UNKNOWN).apply {
                    message = e.message ?: "未知错误，请稍后再试"
                }
            }
        }
    }

    private suspend fun extractErrorBody(e: ResponseException): String? = try {
        e.response.bodyAsText()
    } catch (_: Exception) {
        null
    }

    private fun extractErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val jsonObj = JsonParser.parseString(body).asJsonObject
            jsonObj.get("message")?.asString
                ?: jsonObj.get("msg")?.asString
                ?: jsonObj.get("errorMsg")?.asString
        } catch (_: Exception) {
            body
        }
    }
}
