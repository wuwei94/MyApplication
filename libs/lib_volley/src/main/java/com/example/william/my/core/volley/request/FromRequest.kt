package com.example.william.my.core.volley.request

import com.android.volley.Response

/**
 * 表单参数请求，将 [params] 作为 key-value 参数发送。
 *
 * @param url URL of the request to make
 * @param clazz Relevant class object, for Gson's reflection
 * @param headers Map of request headers
 * @param params Map of request parameters
 */
class FromRequest<T>(
    method: Int,
    url: String,
    clazz: Class<T>,
    headers: MutableMap<String, String>?,
    private val params: MutableMap<String, String>?,
    listener: Response.Listener<T>,
    errorListener: Response.ErrorListener,
) : BaseGsonRequest<T>(method, url, clazz, headers, listener, errorListener) {

    override fun getParams(): MutableMap<String, String> = params ?: super.getParams() ?: mutableMapOf()
}
