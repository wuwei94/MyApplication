package com.example.william.my.module.ipc.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.module.ipc.service.MyMessageService
import java.lang.ref.WeakReference

/**
 * Messenger — 跨进程通信（IPC）
 *
 * Messenger 是 Android 提供的轻量级 IPC 方案，基于 Handler 实现。
 *
 * 核心机制与避坑点：
 * 1. 基于 Handler：底层通过 MessageQueue 调度，无需编写 .aidl 接口
 * 2. 串行处理：消息排队处理，避免并发问题
 * 3. 双向通信：通过 Message.replyTo 实现双向通信
 * 4. 跨进程：支持不同进程间通信
 *
 * 与 AIDL 的区别：
 * - Messenger：消息排队串行处理，无需处理多线程同步，适合轻量级通信
 * - AIDL：并行处理，功能强大，适合复杂通信
 *
 * 官方参考：
 * https://developer.android.com/guide/components/bound-services
 */
class MessengerActivity : BasicResponseActivity() {

    private var serviceMessenger: Messenger? = null
    private var clientMessenger: Messenger? = null
    private var serviceConnection: ServiceConnection? = null

    private class ClientHandler(activity: MessengerActivity) : Handler(Looper.getMainLooper()) {

        private val weakReference: WeakReference<MessengerActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = weakReference.get() ?: return
            if (msg.what == MSG_CODE_SEND_TO_ACTIVITY) {
                val value = msg.data.getString(MSG_SEND_KEY)
                activity.appendLog("✓ 收到回复：$value")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Messenger 跨进程通信\n\n点击下方按钮向 Service 发送消息")
        initMessenger()
    }

    override fun buildList(): ArrayList<String> = arrayListOf("发送消息给 Service")

    override fun onRecyclerClick(position: Int, string: String) {
        sendMessage()
    }

    private fun initMessenger() {
        val clientHandler = ClientHandler(this)
        clientMessenger = Messenger(clientHandler)
    }

    /**
     * 通过 `Messenger.send()` 向服务端发送 `Message`，并携带 `replyTo` 用于接收异步回执。
     */
    private fun sendMessage() {
        serviceMessenger?.let { service ->
            val message = Message.obtain().apply {
                what = MSG_CODE_SEND_TO_SERVICE
                data = Bundle().apply {
                    putString(MSG_SEND_KEY, MSG_SEND_MESSAGE)
                }
                replyTo = clientMessenger
            }
            service.send(message)
            appendLog("→ 发送消息给 Service")
        } ?: appendLog("✗ Service 未连接，请稍候")
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    /**
     * 绑定远程 `MyMessageService` 获取服务端 `Messenger` 并触发首次消息发送。
     */
    private fun bindService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                serviceMessenger = Messenger(iBinder)
                appendLog("✓ Service 已连接")
                sendMessage()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                serviceMessenger = null
                appendLog("✗ Service 连接断开")
            }
        }

        serviceConnection?.let { conn ->
            bindService(
                Intent(this@MessengerActivity, MyMessageService::class.java),
                conn,
                BIND_AUTO_CREATE,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    private fun unbindService() {
        serviceConnection?.let { conn ->
            unbindService(conn)
        }
    }

    companion object {
        const val MSG_CODE_SEND_TO_SERVICE = 1
        const val MSG_CODE_SEND_TO_ACTIVITY = 2
        const val MSG_SEND_KEY = "MSG_SEND_KEY"
        const val MSG_SEND_MESSAGE = "Hello from client"
    }
}
