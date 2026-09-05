package com.example.william.my.basic.basic_shared.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.william.my.core.base.app.BaseApp

object Utils {

    private const val TAG = "Utils"
    private val mainHandler = Handler(Looper.getMainLooper())

    // /////////////////////////////////////////////////////////////////////////
    // Logcat
    // /////////////////////////////////////////////////////////////////////////

    // 规定每段显示的长度
    private const val MAX_LENGTH = 4000

    fun logcat(msg: String) {
        logcat(TAG, msg)
    }

    fun logcat(tag: String, msg: String) {
        var index = 0
        while (index < msg.length) {
            val end = (index + MAX_LENGTH).coerceAtMost(msg.length)
            val temp = msg.substring(index, end)
            index += MAX_LENGTH
            Log.e(tag, temp.trim { it <= ' ' })
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Toast
    // /////////////////////////////////////////////////////////////////////////

    fun toast(msg: String?) {
        if (msg.isNullOrEmpty()) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(BaseApp.app, msg, Toast.LENGTH_SHORT).show()
        } else {
            mainHandler.post {
                Toast.makeText(BaseApp.app, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
