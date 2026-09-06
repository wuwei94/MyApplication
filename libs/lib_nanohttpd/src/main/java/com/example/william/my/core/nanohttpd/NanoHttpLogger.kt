package com.example.william.my.core.nanohttpd

import android.util.Log

/**
 * NanoHTTPD 日志工具类
 */
object NanoHttpLogger {

    private const val TAG = "NanoHTTPD"

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
