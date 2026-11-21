package com.example.william.my.module.async.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity

/**
 * HandlerThread — 子线程通信
 *
 * HandlerThread 是一个自带 Looper 的线程，用于在子线程中处理消息。
 *
 * 核心机制与避坑点：
 * 1. 自带 Looper：内部维护消息队列，可重复发送消息
 * 2. 线程安全：消息按顺序处理，避免并发问题
 * 3. 生命周期管理：需手动调用 quit() 释放资源
 * 4. 适合场景：需要长时间运行的后台任务
 *
 * 与普通 Thread 的区别：
 * - 普通 Thread：每次任务需创建新线程，无法重复使用
 * - HandlerThread：自带 Looper，可重复发送消息，线程复用
 *
 * 官方参考：
 * https://developer.android.com/reference/android/os/HandlerThread
 */
class HandlerThreadActivity : BasicResponseActivity() {

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("HandlerThread — 子线程通信")
        initHandlerThread()
    }

    /**
     * 启动 HandlerThread 并创建绑定其 Looper 的 Handler 接收消息
     */
    private fun initHandlerThread() {
        handlerThread = HandlerThread("DemoHandlerThread").apply { start() }

        handler = object : Handler(handlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_FROM_MAIN -> appendLog("onMessageReceived — 收到主线程消息")
                    MSG_FROM_CHILD -> appendLog("onMessageReceived — 收到子线程消息")
                }
            }
        }
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. sendMessage — 主线程发送",
        "2. sendMessage — 子线程发送",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> handler?.sendEmptyMessage(MSG_FROM_MAIN)
            1 -> Thread { handler?.sendEmptyMessage(MSG_FROM_CHILD) }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread?.quit()
    }

    companion object {
        private const val MSG_FROM_MAIN = 1
        private const val MSG_FROM_CHILD = 2
    }
}
