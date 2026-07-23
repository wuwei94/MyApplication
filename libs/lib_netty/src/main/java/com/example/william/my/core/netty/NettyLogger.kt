package com.example.william.my.core.netty

import android.util.Log

object NettyLogger {

    private const val TAG = "Netty"

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
