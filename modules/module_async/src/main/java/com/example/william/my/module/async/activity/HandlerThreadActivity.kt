package com.example.william.my.module.async.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * HandlerThread — 子线程通信
 *
 * HandlerThread 是一个自带 Looper 的线程，用于在子线程中处理消息。
 *
 * 核心特性：
 * 1. 自带 Looper：内部维护消息队列，可重复发送消息
 * 2. 线程安全：消息按顺序处理，避免并发问题
 * 3. 生命周期管理：需手动调用 quit() 释放资源
 * 4. 适合场景：需要长时间运行的后台任务
 *
 * 与普通 Thread 的区别：
 * - 普通 Thread：每次任务需创建新线程，无法重复使用
 * - HandlerThread：自带 Looper，可重复发送消息，线程复用
 *
 * 基本用法：
 * ```kotlin
 * // 创建并启动 HandlerThread
 * val handlerThread = HandlerThread("MyThread")
 * handlerThread.start()
 *
 * // 创建 Handler
 * val handler = object : Handler(handlerThread.looper) {
 *     override fun handleMessage(msg: Message) {
 *         // 处理消息
 *     }
 * }
 *
 * // 发送消息
 * handler.sendEmptyMessage(MSG_CODE)
 *
 * // 释放资源
 * handlerThread.quit()
 * ```
 *
 * 适用场景：
 * - 需要长时间运行的后台任务
 * - 多个消息按顺序处理
 * - 避免频繁创建/销毁线程
 */
@Route(path = RouterPath.Async.HandlerThread)
class HandlerThreadActivity : BasicResponseActivity() {

    private var mHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("HandlerThread — 子线程通信")
        initHandlerThread()
    }

    private fun initHandlerThread() {
        mHandlerThread = HandlerThread("DemoHandlerThread").apply { start() }

        mHandler = object : Handler(mHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_FROM_MAIN -> appendLog("onMessageReceived — 收到主线程消息")
                    MSG_FROM_CHILD -> appendLog("onMessageReceived — 收到子线程消息")
                }
            }
        }
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "sendMessage — 主线程发送",
        "sendMessage — 子线程发送",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> mHandler?.sendEmptyMessage(MSG_FROM_MAIN)
            1 -> Thread { mHandler?.sendEmptyMessage(MSG_FROM_CHILD) }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandlerThread?.quit()
    }

    companion object {
        private const val MSG_FROM_MAIN = 1
        private const val MSG_FROM_CHILD = 2
    }
}
