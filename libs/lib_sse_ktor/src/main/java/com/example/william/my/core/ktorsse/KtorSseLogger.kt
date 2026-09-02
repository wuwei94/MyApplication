package com.example.william.my.core.ktorsse

import android.util.Log

object KtorSseLogger {

    private const val TAG = "KtorSse"
    private var isLogEnabled = true

    fun setLogEnabled(enabled: Boolean) {
        isLogEnabled = enabled
    }

    fun debug(message: String) {
        if (isLogEnabled) {
            Log.d(TAG, message)
        }
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (isLogEnabled) {
            Log.e(TAG, message, throwable)
        }
    }
}
