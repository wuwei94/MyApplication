package com.example.william.my.core.volley.builder

import android.content.Context
import com.android.volley.Request.Method
import com.example.william.my.core.volley.VolleySingleton
import com.example.william.my.core.volley.listener.VolleyListener
import com.example.william.my.core.volley.request.FromRequest
import com.example.william.my.core.volley.request.JsonRequest
import org.json.JSONObject

/**
 * Volley 请求构建器。
 *
 * 使用方式：
 * ```
 * VolleyBuilder<MyResponse>()
 *     .url("https://api.example.com/data")
 *     .clazz(MyResponse::class.java)
 *     .post()
 *     .addHeader("Authorization", "Bearer token")
 *     .addJsonObject(jsonBody)
 *     .build(context, listener)
 * ```
 */
class VolleyBuilder<T> {

    private lateinit var url: String
    private lateinit var clazz: Class<T>
    private var method: Int = Method.GET
    private val header: MutableMap<String, String> = mutableMapOf()
    private val parameter: MutableMap<String, String> = mutableMapOf()
    private var jsonObject: JSONObject? = null
    private var tag: String = TAG_GENERATOR.getAndIncrement().toString()

    fun url(api: String): VolleyBuilder<T> {
        url = api
        return this
    }

    fun clazz(clazz: Class<T>): VolleyBuilder<T> {
        this.clazz = clazz
        return this
    }

    fun get(): VolleyBuilder<T> {
        method = Method.GET
        return this
    }

    fun post(): VolleyBuilder<T> {
        method = Method.POST
        return this
    }

    fun delete(): VolleyBuilder<T> {
        method = Method.DELETE
        return this
    }

    fun put(): VolleyBuilder<T> {
        method = Method.PUT
        return this
    }

    fun addHeader(key: String, value: String): VolleyBuilder<T> {
        header[key] = value
        return this
    }

    fun addHeader(header: MutableMap<String, String>): VolleyBuilder<T> {
        this.header.putAll(header)
        return this
    }

    fun addParam(key: String, value: String): VolleyBuilder<T> {
        parameter[key] = value
        return this
    }

    fun addParams(params: MutableMap<String, String>): VolleyBuilder<T> {
        parameter.putAll(params)
        return this
    }

    fun addJsonObject(jsonObject: JSONObject): VolleyBuilder<T> {
        this.jsonObject = jsonObject
        return this
    }

    fun tag(tag: String): VolleyBuilder<T> {
        this.tag = tag
        return this
    }

    /**
     * 构建请求并提交到 RequestQueue。
     *
     * @param context 用于获取 applicationContext，避免 Activity 泄漏
     * @param listener 响应回调
     */
    fun build(context: Context, listener: VolleyListener<T>) {
        check(::url.isInitialized) { "url 未设置，请调用 .url()" }
        check(::clazz.isInitialized) { "clazz 未设置，请调用 .clazz()" }

        val finalUrl = buildUrl()
        val request = if (jsonObject != null) {
            JsonRequest(
                method,
                finalUrl,
                clazz,
                header,
                jsonObject,
                listener.listener,
                listener.errorListener,
            )
        } else {
            FromRequest(
                method,
                finalUrl,
                clazz,
                header,
                parameter,
                listener.listener,
                listener.errorListener,
            )
        }
        request.tag = tag
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    /**
     * GET/DELETE 将 params 拼到 URL 上，POST/PUT 直接返回原 URL。
     */
    private fun buildUrl(): String {
        if (parameter.isEmpty()) return url
        if (method != Method.GET && method != Method.DELETE) return url
        val separator = if (url.contains("?")) "&" else "?"
        val query = parameter.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$url$separator$query"
    }

    companion object {
        private val TAG_GENERATOR = java.util.concurrent.atomic.AtomicInteger(0)
    }
}
