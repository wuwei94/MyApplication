package com.example.william.my.core.volley.builder

import com.android.volley.Request.Method
import com.example.william.my.core.volley.VolleyHelper
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class VolleyBuilder<T> {

    private lateinit var url: String
    private lateinit var clazz: Class<T>
    private var method: Int = Method.GET
    private var header: MutableMap<String, String> = mutableMapOf()
    private var parameter: MutableMap<String, String> = mutableMapOf()
    private var jsonObject: JSONObject? = null
    private var tag: String = TAG_GENERATOR.getAndIncrement().toString()

    fun getUrl(): String = url
    fun getClazz(): Class<T> = clazz
    fun getMethod(): Int = method
    fun getHeader(): MutableMap<String, String> = header
    fun getParameter(): MutableMap<String, String> = parameter
    fun getJsonObject(): JSONObject? = jsonObject
    fun getTag(): String = tag

    fun url(api: String): VolleyBuilder<T> {
        this.url = api
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
        this.header = header
        return this
    }

    fun addParam(key: String, value: String): VolleyBuilder<T> {
        parameter[key] = value
        return this
    }

    fun addParams(params: MutableMap<String, String>): VolleyBuilder<T> {
        this.parameter = params
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

    fun build(): VolleyHelper<T> {
        return VolleyHelper(this)
    }

    companion object {
        private val TAG_GENERATOR = AtomicInteger(0)
    }
}
