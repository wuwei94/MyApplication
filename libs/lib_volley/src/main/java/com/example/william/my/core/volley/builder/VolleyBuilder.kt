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

    private lateinit var mUrl: String
    private lateinit var mClazz: Class<T>
    private var mMethod: Int = Method.GET
    private val mHeader: MutableMap<String, String> = mutableMapOf()
    private val mParameter: MutableMap<String, String> = mutableMapOf()
    private var mJsonObject: JSONObject? = null
    private var mTag: String = TAG_GENERATOR.getAndIncrement().toString()

    fun url(api: String): VolleyBuilder<T> {
        mUrl = api
        return this
    }

    fun clazz(clazz: Class<T>): VolleyBuilder<T> {
        mClazz = clazz
        return this
    }

    fun get(): VolleyBuilder<T> {
        mMethod = Method.GET
        return this
    }

    fun post(): VolleyBuilder<T> {
        mMethod = Method.POST
        return this
    }

    fun delete(): VolleyBuilder<T> {
        mMethod = Method.DELETE
        return this
    }

    fun put(): VolleyBuilder<T> {
        mMethod = Method.PUT
        return this
    }

    fun addHeader(key: String, value: String): VolleyBuilder<T> {
        mHeader[key] = value
        return this
    }

    fun addHeader(header: MutableMap<String, String>): VolleyBuilder<T> {
        mHeader.putAll(header)
        return this
    }

    fun addParam(key: String, value: String): VolleyBuilder<T> {
        mParameter[key] = value
        return this
    }

    fun addParams(params: MutableMap<String, String>): VolleyBuilder<T> {
        mParameter.putAll(params)
        return this
    }

    fun addJsonObject(jsonObject: JSONObject): VolleyBuilder<T> {
        mJsonObject = jsonObject
        return this
    }

    fun tag(tag: String): VolleyBuilder<T> {
        mTag = tag
        return this
    }

    /**
     * 构建请求并提交到 RequestQueue。
     *
     * @param context 用于获取 applicationContext，避免 Activity 泄漏
     * @param listener 响应回调
     */
    fun build(context: Context, listener: VolleyListener<T>) {
        check(::mUrl.isInitialized) { "url 未设置，请调用 .url()" }
        check(::mClazz.isInitialized) { "clazz 未设置，请调用 .clazz()" }

        val finalUrl = buildUrl()
        val request = if (mJsonObject != null) {
            JsonRequest(
                mMethod,
                finalUrl,
                mClazz,
                mHeader,
                mJsonObject,
                listener.mListener,
                listener.mErrorListener,
            )
        } else {
            FromRequest(
                mMethod,
                finalUrl,
                mClazz,
                mHeader,
                mParameter,
                listener.mListener,
                listener.mErrorListener,
            )
        }
        request.tag = mTag
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    /**
     * GET/DELETE 将 params 拼到 URL 上，POST/PUT 直接返回原 URL。
     */
    private fun buildUrl(): String {
        if (mParameter.isEmpty()) return mUrl
        if (mMethod != Method.GET && mMethod != Method.DELETE) return mUrl
        val separator = if (mUrl.contains("?")) "&" else "?"
        val query = mParameter.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$mUrl$separator$query"
    }

    companion object {
        private val TAG_GENERATOR = java.util.concurrent.atomic.AtomicInteger(0)
    }
}
