package com.example.william.my.core.okhttp.utils

import android.util.Log

/**
 * HTTP 日志工具
 */
object HttpLogger {

    private const val TAG = "OkHttp"

    fun debug(msg: String) {
        Log.d(TAG, msg)
    }

    fun error(msg: String) {
        Log.e(TAG, msg)
    }
}
