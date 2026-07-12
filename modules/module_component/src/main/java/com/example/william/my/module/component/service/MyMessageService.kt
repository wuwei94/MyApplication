package com.example.william.my.module.component.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.example.william.my.module.component.activity.MessengerActivity
import java.lang.ref.WeakReference

/**
 * Messenger Service（跨进程通信）
 *
 * 通过 Messenger + Handler 实现跨进程消息通信。
 * 与 AIDL 的区别：AIDL 支持并发调用，Messenger 是串行处理（单线程 Handler）。
 */
class MyMessageService : Service() {

    private var clientMessenger: Messenger? = null
    private lateinit var serviceMessenger: Messenger

    override fun onCreate() {
        super.onCreate()
        serviceMessenger = Messenger(ServiceHandler(this))
    }

    override fun onBind(intent: Intent): IBinder = serviceMessenger.binder

    private fun sendMessage(client: Messenger?, msg: Message) {
        client?.send(msg)
    }

    private class ServiceHandler(service: MyMessageService) : Handler(Looper.getMainLooper()) {

        private val weakReference = WeakReference(service)

        override fun handleMessage(msg: Message) {
            val service = weakReference.get() ?: return
            if (msg.what == MessengerActivity.MSG_CODE_SEND_TO_SERVICE) {
                service.clientMessenger = msg.replyTo
                val value = msg.data.getString(MessengerActivity.MSG_SEND_KEY)

                val reply = Message.obtain(null, MessengerActivity.MSG_CODE_SEND_TO_ACTIVITY).apply {
                    data = Bundle().apply {
                        putString(MessengerActivity.MSG_SEND_KEY, value)
                    }
                }
                service.sendMessage(service.clientMessenger, reply)
            }
        }
    }
}
