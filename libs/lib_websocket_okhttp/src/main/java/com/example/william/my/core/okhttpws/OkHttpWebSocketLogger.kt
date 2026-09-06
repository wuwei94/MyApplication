package com.example.william.my.core.okhttpws

import android.util.Log

/**
 * OkHttp WebSocket 日志工具类
 */
object OkHttpWebSocketLogger {

    private const val TAG = "OkHttpWebSocket"

    private var isDebug = false

    fun setDebug(enable: Boolean) {
        isDebug = enable
    }

    fun debug(msg: String) {
        if (isDebug) {
            Log.d(TAG, msg)
        }
    }

    fun error(msg: String, t: Throwable? = null) {
        Log.e(TAG, msg, t)
    }
}
