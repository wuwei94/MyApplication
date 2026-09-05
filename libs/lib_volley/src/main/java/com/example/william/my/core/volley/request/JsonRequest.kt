package com.example.william.my.core.volley.request

import com.android.volley.Response
import com.android.volley.VolleyLog
import org.json.JSONObject
import java.io.UnsupportedEncodingException

/**
 * JSON body 请求，将 [jsonObject] 序列化后作为请求体发送。
 *
 * @param url URL of the request to make
 * @param clazz Relevant class object, for Gson's reflection
 * @param headers Map of request headers
 * @param jsonObject JSON body to send
 */
class JsonRequest<T>(
    method: Int,
    url: String,
    clazz: Class<T>,
    headers: MutableMap<String, String>?,
    private val jsonObject: JSONObject?,
    listener: Response.Listener<T>,
    errorListener: Response.ErrorListener,
) : BaseGsonRequest<T>(method, url, clazz, headers, listener, errorListener) {

    override fun getBody(): ByteArray? {
        try {
            return jsonObject.toString().toByteArray(charset("utf-8"))
        } catch (uee: UnsupportedEncodingException) {
            VolleyLog.wtf(
                "Unsupported Encoding while trying to get the bytes of %s using %s",
                jsonObject.toString(),
                "utf-8",
            )
            return null
        }
    }
}
