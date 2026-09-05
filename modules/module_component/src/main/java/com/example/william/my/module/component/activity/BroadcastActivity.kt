package com.example.william.my.module.component.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.ref.WeakReference

/**
 * BroadcastReceiver — 广播接收器
 *
 * BroadcastReceiver 是 Android 四大组件之一，用于接收和处理广播消息。
 *
 * 核心特性：
 * 1. 系统广播：接收系统事件（如电量变化、网络状态变化）
 * 2. 自定义广播：应用内或应用间发送自定义消息
 * 3. 动态注册：在代码中注册，生命周期跟随注册者
 * 4. 静态注册：在 AndroidManifest 中注册，应用未启动也能接收
 *
 * 注册方式：
 * 1. 动态注册：registerReceiver() / unregisterReceiver()
 * 2. 静态注册：在 AndroidManifest 中声明 receiver
 *
 * 基本用法：
 * ```kotlin
 * // 动态注册
 * val receiver = MyReceiver()
 * val filter = IntentFilter("com.example.MY_ACTION")
 * registerReceiver(receiver, filter)
 *
 * // 发送广播
 * val intent = Intent("com.example.MY_ACTION")
 * sendBroadcast(intent)
 *
 * // 注销
 * unregisterReceiver(receiver)
 * ```
 *
 * 注意事项：
 * - Android 13+ 需要指定 RECEIVER_NOT_EXPORTED 标志
 * - 使用 WeakReference 防止内存泄漏
 * - 避免在广播中执行耗时操作
 *
 * 适用场景：
 * - 系统事件监听
 * - 应用内消息传递
 * - 跨组件通信
 */
@Route(path = RouterPath.Component.Broadcast)
class BroadcastActivity : BasicResponseActivity() {

    private var mMessageReceiver: MessageReceiver? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("BroadcastReceiver\n\n点击下方按钮发送广播")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("发送广播")

    override fun onRecyclerClick(position: Int, string: String) {
        sendBroadcast()
    }

    private fun sendBroadcast() {
        val intent = Intent(MessageReceiver.ACTION_UPDATE).apply {
            setPackage(packageName)
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
            ContextCompat.registerReceiver(this, mMessageReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(this, mMessageReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
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
