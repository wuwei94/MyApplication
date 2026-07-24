package com.example.william.my.core.volley.request

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * 基于 Gson 的 Volley Request 基类，提供通用的 JSON 解析逻辑。
 */
abstract class BaseGsonRequest<T>(
    method: Int,
    url: String,
    private val clazz: Class<T>,
    private val headers: MutableMap<String, String>?,
    private val listener: Response.Listener<T>,
    errorListener: Response.ErrorListener
) : Request<T>(method, url, errorListener) {

    override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()

    override fun deliverResponse(response: T) = listener.onResponse(response)

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        return try {
            val json = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers))
            )
            Response.success(
                gson.fromJson(json, clazz),
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            Response.error(ParseError(e))
        }
    }

    companion object {
        private val gson = Gson()
    }
}
