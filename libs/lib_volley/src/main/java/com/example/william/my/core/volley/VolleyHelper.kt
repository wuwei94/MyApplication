package com.example.william.my.core.volley

import android.content.Context
import com.example.william.my.core.volley.builder.VolleyBuilder
import com.example.william.my.core.volley.listener.VolleyListener
import com.example.william.my.core.volley.request.FromRequest
import com.example.william.my.core.volley.request.JsonRequest

class VolleyHelper<T>(private val builder: VolleyBuilder<T>) {

    fun enqueue(content: Context, responseListener: VolleyListener<T>) {
        val request = if (builder.getJsonObject() != null) {
            JsonRequest(
                builder.getMethod(),
                builder.getUrl(),
                builder.getClazz(),
                builder.getHeader(),
                builder.getJsonObject(),
                responseListener.mListener,
                responseListener.mErrorListener
            )
        } else {
            FromRequest(
                builder.getMethod(),
                builder.getUrl(),
                builder.getClazz(),
                builder.getHeader(),
                builder.getParameter(),
                responseListener.mListener,
                responseListener.mErrorListener
            )
        }
        request.tag = builder.getTag()
        VolleySingleton.getInstance(content).addToRequestQueue(request)
    }

    fun cancel(content: Context, tag: String) {
        VolleySingleton.getInstance(content).cancel(tag)
    }

    companion object {
        fun <T> builder(): VolleyBuilder<T> {
            return VolleyBuilder()
        }
    }
}
