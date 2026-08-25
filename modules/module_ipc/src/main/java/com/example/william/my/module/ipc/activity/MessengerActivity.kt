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
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ipc.service.MyMessageService
import java.lang.ref.WeakReference

/**
 * Messenger — 跨进程通信（IPC）
 *
 * Messenger 是 Android 提供的轻量级 IPC 方案，基于 Handler 实现。
 *
 * 核心特性：
 * 1. 简单易用：基于 Handler，学习成本低
 * 2. 串行处理：消息排队处理，避免并发问题
 * 3. 双向通信：通过 Message.replyTo 实现双向通信
 * 4. 跨进程：支持不同进程间通信
 *
 * 与 AIDL 的区别：
 * - Messenger：串行处理，简单易用，适合轻量级通信
 * - AIDL：并行处理，功能强大，适合复杂通信
 *
 * 基本用法：
 * ```kotlin
 * // 服务端
 * class MyService : Service() {
 *     private val messenger = Messenger(IncomingHandler())
 *
 *     override fun onBind(intent: Intent): IBinder {
 *         return messenger.binder
 *     }
 * }
 *
 * // 客户端
 * val connection = object : ServiceConnection {
 *     override fun onServiceConnected(name: ComponentName, service: IBinder) {
 *         val messenger = Messenger(service)
 *         val msg = Message.obtain().apply {
 *             what = MSG_CODE
 *             replyTo = clientMessenger  // 设置回复 Messenger
 *         }
 *         messenger.send(msg)
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 跨进程通信
 * - Activity 与 Service 通信
 * - 轻量级 IPC 需求
 */
@Route(path = RouterPath.Ipc.Messenger)
class MessengerActivity : BasicResponseActivity() {

    private var mServiceMessenger: Messenger? = null
    private var mClientMessenger: Messenger? = null
    private var mServiceConnection: ServiceConnection? = null

    private class ClientHandler(activity: MessengerActivity) : Handler(Looper.getMainLooper()) {

        private val weakReference: WeakReference<MessengerActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mActivity = weakReference.get() ?: return
            if (msg.what == MSG_CODE_SEND_TO_ACTIVITY) {
                val value = msg.data.getString(MSG_SEND_KEY)
                mActivity.appendLog("收到回复：$value")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Messenger 跨进程通信\n\n点击下方按钮向 Service 发送消息")
        initMessenger()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("发送消息给 Service")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        sendMessage()
    }

    private fun initMessenger() {
        val clientHandler = ClientHandler(this)
        mClientMessenger = Messenger(clientHandler)
    }

    private fun sendMessage() {
        mServiceMessenger?.let { service ->
            val message = Message.obtain().apply {
                what = MSG_CODE_SEND_TO_SERVICE
                data = Bundle().apply {
                    putString(MSG_SEND_KEY, MSG_SEND_MESSAGE)
                }
                replyTo = mClientMessenger
            }
            service.send(message)
            appendLog("发送消息给 Service")
        } ?: appendLog("Service 未连接，请稍候")
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    private fun bindService() {
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                mServiceMessenger = Messenger(iBinder)
                appendLog("Service 已连接")
                sendMessage()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                mServiceMessenger = null
                appendLog("Service 连接断开")
            }
        }

        mServiceConnection?.let { conn ->
            bindService(
                Intent(this@MessengerActivity, MyMessageService::class.java),
                conn,
                BIND_AUTO_CREATE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    private fun unbindService() {
        mServiceConnection?.let { conn ->
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
