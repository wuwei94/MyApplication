package com.example.william.my.core.retrofit.exception

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLException

/**
 * ApiException
 * 1. HTTP协议错误
 * 4xx/5xx状态码：如401（未授权）、404（资源不存在）、500（服务器内部错误）等，需根据状态码提示用户或重试。
 * 自定义业务错误：服务器返回的success=false或自定义错误码（如code=1001），需解析并统一处理。
 * 2. 网络连接异常
 * ConnectException：无网络或DNS解析失败。
 * SocketTimeoutException：请求超时。
 * SSLException：证书验证失败。
 * 3. 数据解析错误
 * JsonParseException或JSONException：JSON格式与数据模型不匹配。
 * 服务器返回非约定格式（如错误时返回字符串而非对象）。
 * UnknownHostException：域名解析失败。
 * 4. 其他异常
 * CancellationException：协程取消，继续向上传播。
 */
object ExceptionHandler {

    fun handleException(e: Throwable): ApiException {
        if (e is CancellationException) throw e
        val exception: ApiException
        return when (e) {
            is HttpException -> {
                exception = ApiException(e, e.code())
                try {
                    e.response()?.errorBody()?.let { errorBody ->
                        val bodyString = errorBody.string()
                        exception.message = extractErrorMessage(bodyString)
                            ?: "请求错误(${e.code()})"
                    } ?: run {
                        exception.message = "请求错误(${e.code()})"
                    }
                } catch (e1: Exception) {
                    exception.message = "请求错误(${e.code()})"
                }
                exception
            }

            is ServerResultException -> {
                exception = ApiException(e, e.code)
                exception.message = e.message.takeIf { it.isNotBlank() }
                    ?: ApiException.DEFAULT_MESSAGE
                exception
            }

            is ConnectException, is UnknownHostException -> {
                exception = ApiException(e, ApiException.Error.CONNECT_ERROR)
                exception.message = "连接失败，请检查网络设置"
                exception
            }

            is SocketTimeoutException -> {
                exception = ApiException(e, ApiException.Error.TIMEOUT_ERROR)
                exception.message = "请求超时，请稍后再试"
                exception
            }

            is SSLException -> {
                exception = ApiException(e, ApiException.Error.SSL_ERROR)
                exception.message = "证书校验失败，请稍后再试"
                exception
            }

            is JsonParseException, is JSONException -> {
                exception = ApiException(e, ApiException.Error.PARSE_ERROR)
                exception.message = "解析错误，请稍后再试"
                exception
            }

            else -> {
                exception = ApiException(e, ApiException.Error.UNKNOWN)
                exception.message = e.message
                    ?.takeIf { it.isNotBlank() }
                    ?: ApiException.DEFAULT_MESSAGE
                exception
            }
        }
    }

    /**
     * 从 HTTP 错误响应体中提取服务端错误消息。
     *
     * JSON 响应依次读取 `message`、`msg`、`errorMsg`；空响应或没有这些字段时返回 null，
     * 非 JSON 响应则保留原始文本。
     */
    fun extractErrorMessage(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val jsonObj = JsonParser.parseString(body).asJsonObject
            (
                jsonObj.get("message")?.asString
                    ?: jsonObj.get("msg")?.asString
                    ?: jsonObj.get("errorMsg")?.asString
                )
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            body
        }
    }
}
