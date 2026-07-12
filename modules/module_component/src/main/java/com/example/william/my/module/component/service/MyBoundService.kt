package com.example.william.my.module.component.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 普通 Service（Started + Bound）
 *
 * Started 模式：通过 startService() 启动，独立运行，通过 Intent 传递消息
 * Bound 模式：通过 bindService() 绑定，客户端通过 LocalBinder 直接调用方法
 */
class MyBoundService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MyBoundService = this@MyBoundService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Started Service 收到消息"
        Utils.toast(message)
        return START_STICKY
    }

    fun getMessage(): String = "来自 BoundService 的消息"

    companion object {
        const val EXTRA_MESSAGE = "message"
    }
}
