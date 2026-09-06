package com.example.william.my.core.okttpsse

import android.util.Log

/**
 * OkHttp SSE 日志工具类
 */
object OkHttpSseLogger {
    private const val TAG = "OkHttpSse"

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
