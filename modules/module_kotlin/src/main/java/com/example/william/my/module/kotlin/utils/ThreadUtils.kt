package com.example.william.my.module.kotlin.utils

import android.os.Looper
import com.example.william.my.basic.basic_shared.utils.Utils

object ThreadUtils {

    private val TAG = this.javaClass.simpleName

    fun isMainThread(name: String) {
        val isMainThread = Looper.myLooper() == Looper.getMainLooper()
        Utils.logcat(TAG, "$name isMainThread : $isMainThread")
    }
}
