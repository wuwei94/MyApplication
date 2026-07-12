package com.example.william.my.module.component.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.ref.WeakReference

/**
 * BroadcastReceiver 广播注册与发送
 *
 * 演示动态注册广播接收器、发送广播、接收并处理广播消息的完整流程。
 * 使用 WeakReference 防止内部类持有 Activity 导致内存泄漏。
 * Android 13+ 需要指定 RECEIVER_NOT_EXPORTED 标志。
 */
@Route(path = RouterPath.Component.Broadcast)
class BroadcastActivity : BasicResponseActivity() {

    private var mMessageReceiver: MessageReceiver? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("BroadcastReceiver\n\n点击下方按钮发送广播")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("发送广播")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        sendBroadcast()
    }

    private fun sendBroadcast() {
        val intent = Intent(MessageReceiver.ACTION_UPDATE).apply {
            putExtra("message", MessageReceiver.ACTION_UPDATE)
        }
        sendBroadcast(intent)
        appendLog("发送广播：${MessageReceiver.ACTION_UPDATE}")
    }

    override fun onStart() {
        super.onStart()
        registerReceiver()
    }

    private fun registerReceiver() {
        mMessageReceiver = MessageReceiver(this)

        val intentFilter = IntentFilter(MessageReceiver.ACTION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mMessageReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mMessageReceiver, intentFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun unregisterReceiver() {
        mMessageReceiver?.let {
            unregisterReceiver(it)
        }
    }

    /**
     * 接收广播并更新 UI。
     * 使用 WeakReference 防止内存泄漏。
     */
    class MessageReceiver(activity: BroadcastActivity?) : BroadcastReceiver() {

        private val weakReference: WeakReference<BroadcastActivity?> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            weakReference.get()?.let {
                it.appendLog("收到广播：${intent.getStringExtra("message")}")
            }
        }

        companion object {
            const val ACTION_UPDATE = "com.example.broadcast"
        }
    }
}
