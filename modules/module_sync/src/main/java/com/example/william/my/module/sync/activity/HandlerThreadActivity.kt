package com.example.william.my.module.sync.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * HandlerThread — 子线程通信演示
 *
 * HandlerThread 是一个自带 Looper 的线程，可以异步处理消息
 * 与普通 Thread 的区别：可以重复发送消息，不需要每次创建新线程
 */
@Route(path = RouterPath.Sync.HandlerThread)
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

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "sendMessage — 主线程发送",
            "sendMessage — 子线程发送"
        )
    }

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
