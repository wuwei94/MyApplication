package com.example.william.my.core.javaws

import android.util.Log

/**
 * Java-WebSocket 日志工具类
 */
object JavaWebSocketLogger {

    private const val TAG = "JavaWebSocket"

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
