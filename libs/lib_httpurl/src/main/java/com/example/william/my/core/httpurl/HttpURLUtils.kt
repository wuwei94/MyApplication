package com.example.william.my.core.httpurl

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 基于 HttpURLConnection 的网络请求工具类
 */
object HttpURLUtils {

    private const val TAG = "HttpURLUtils"

    const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=utf-8"

    private const val DEFAULT_TIMEOUT = 3000

    fun get(
        url: String,
        params: Map<String, String> = emptyMap(),
        listener: Listener<String>,
        errorListener: ErrorListener,
        connectTimeout: Int = DEFAULT_TIMEOUT,
        readTimeout: Int = DEFAULT_TIMEOUT,
    ) {
        val finalUrl = if (params.isEmpty()) {
            url
        } else {
            buildString {
                append(url)
                append(if (url.contains("?")) "&" else "?")
                append(encodedParams(params))
            }
        }
        request(finalUrl, "GET", null, null, listener, errorListener, connectTimeout, readTimeout)
    }

    fun postForm(
        url: String,
        params: Map<String, String>,
        listener: Listener<String>,
        errorListener: ErrorListener,
        connectTimeout: Int = DEFAULT_TIMEOUT,
        readTimeout: Int = DEFAULT_TIMEOUT,
    ) {
        request(url, "POST", CONTENT_TYPE_FORM, encodedParams(params).toByteArray(), listener, errorListener, connectTimeout, readTimeout)
    }

    fun postJson(
        url: String,
        json: JSONObject,
        listener: Listener<String>,
        errorListener: ErrorListener,
        connectTimeout: Int = DEFAULT_TIMEOUT,
        readTimeout: Int = DEFAULT_TIMEOUT,
    ) {
        request(url, "POST", CONTENT_TYPE_JSON, json.toString().toByteArray(), listener, errorListener, connectTimeout, readTimeout)
    }

    private fun request(
        url: String,
        method: String,
        contentType: String?,
        body: ByteArray?,
        listener: Listener<String>,
        errorListener: ErrorListener,
        connectTimeout: Int,
        readTimeout: Int,
    ) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection)
            conn.requestMethod = method
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType)
            }

            Log.d(TAG, "Request Method: ${conn.requestMethod}")
            Log.d(TAG, "Request Headers: ${conn.requestProperties}")

            if (body != null) {
                conn.outputStream.use { os ->
                    os.write(body)
                    os.flush()
                }
            }

            Log.d(TAG, "Response Headers: ${conn.headerFields}")

            val code = conn.responseCode
            Log.d(TAG, "Response Code: $code")

            val response = StringBuilder()
            if (code in 200..299) {
                conn.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { response.append(it).append("\n") }
                }
                Log.d(TAG, "Response Body: $response")
            }

            listener.onResponse(response.toString())
        } catch (e: Exception) {
            errorListener.onErrorResponse(e)
        } finally {
            conn?.disconnect()
        }
    }

    private fun encodedParams(params: Map<String, String>): String = buildString {
        for ((key, value) in params) {
            if (isNotEmpty()) append('&')
            append(URLEncoder.encode(key, "UTF-8"))
            append('=')
            append(URLEncoder.encode(value, "UTF-8"))
        }
    }

    fun interface Listener<T> {
        fun onResponse(response: T)
    }

    fun interface ErrorListener {
        fun onErrorResponse(error: Exception?)
    }
}
