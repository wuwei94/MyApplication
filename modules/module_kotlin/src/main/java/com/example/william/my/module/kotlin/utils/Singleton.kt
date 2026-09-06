package com.example.william.my.module.kotlin.utils

import android.app.Application
import android.widget.Toast

/**
 * 单例
 *
 * 演示 Kotlin 单例的多种实现方式。
 */
class Singleton private constructor(private var context: Application) {

    companion object {

        private var instance: Singleton? = null

        fun getInstance(context: Application) = instance ?: synchronized(this) {
            instance ?: Singleton(context).also {
                instance = it
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
