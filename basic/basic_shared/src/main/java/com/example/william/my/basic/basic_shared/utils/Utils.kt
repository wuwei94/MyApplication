package com.example.william.my.basic.basic_shared.utils

import android.util.Log
import android.widget.Toast
import com.example.william.my.core.base.app.BaseApp

object Utils {

    private val TAG = this.javaClass.simpleName


    ///////////////////////////////////////////////////////////////////////////
    // Logcat
    ///////////////////////////////////////////////////////////////////////////

    //规定每段显示的长度
    private const val MAX_LENGTH = 4000

    fun logcat(msg: String) {
        logcat(TAG, msg)
    }

    fun logcat(tag: String, msg: String) {
        var temp: String
        var index = 0
        while (index < msg.length) {
            // java的字符不允许指定超过总的长度end
            temp = if (msg.length <= index + MAX_LENGTH) {
                msg.substring(index)
            } else {
                msg.substring(index, index + MAX_LENGTH)
            }
            index += MAX_LENGTH
            Log.e(tag, temp.trim { it <= ' ' })
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Toast
    ///////////////////////////////////////////////////////////////////////////

    fun toast(msg: String?) {
        Toast.makeText(BaseApp.app, msg, Toast.LENGTH_SHORT).show()
    }
}