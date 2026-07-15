package com.example.william.my.module.system.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * NotificationChannel 通知渠道创建与通知发送
 *
 * Android 8.0 (API 26) 起，所有通知必须绑定到 NotificationChannel。
 * 本示例演示：自动创建渠道并发送通知。
 */
@Route(path = RouterPath.System.Notification)
class NotificationActivity : BasicResponseActivity() {

    private var mNotificationManager: NotificationManager? = null
    private var notificationId = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        showResponse("NotificationChannel 通知渠道\n\n点击下方按钮发送通知")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("发送通知")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        sendNotification()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // IMPORTANCE_HIGH: 弹出悬浮通知 + 提示音，适合即时消息
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        channel.setShowBadge(true)
        channel.description = CHANNEL_DESCRIPTION
        mNotificationManager?.createNotificationChannel(channel)
    }

    private fun sendNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.shared_ic_launcher)
            .setContentTitle("测试通知")
            .setContentText("这是一条测试通知")
            .setAutoCancel(true)
            .build()

        mNotificationManager?.notify(++notificationId, notification)
        appendLog("发送通知成功（ID: $notificationId）")
    }

    companion object {
        private const val CHANNEL_ID = "demo_channel"
        private const val CHANNEL_NAME = "示例渠道"
        private const val CHANNEL_DESCRIPTION = "用于演示通知发送"
    }
}
