package com.example.william.my.module.systemservice.activity.notification

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
 * NotificationChannel — 通知渠道创建与通知发送
 *
 * Android 8.0（API 26）起，所有通知必须绑定到 NotificationChannel。
 * 用户可在系统设置中按渠道精细控制通知行为（振动、角标、重要性等级等）。
 *
 * 核心机制与避坑点：
 * 1. 渠道创建：NotificationChannel（id + name + importance）
 *    - IMPORTANCE_HIGH：弹出悬浮通知 + 提示音（适合即时消息）
 *    - IMPORTANCE_DEFAULT：有提示音但不弹出（适合一般推送）
 *    - IMPORTANCE_LOW：无提示音无弹出（适合后台进度）
 * 2. 渠道注册：NotificationManager.createNotificationChannel() 注册渠道（重复调用安全）
 * 3. 通知构建：通过 NotificationCompat.Builder(context, channelId) 构建通知并绑定渠道
 * 4. 通知发送：NotificationManager.notify(id, notification) 发送通知，相同 id 会替换旧通知
 *
 * https://developer.android.com/develop/ui/views/notifications
 */
@Route(path = RouterPath.SystemService.Notification)
class NotificationActivity : BasicResponseActivity() {

    private var notificationManager: NotificationManager? = null
    private var notificationId = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        showDescription("NotificationChannel 通知渠道\n\n点击下方按钮发送通知")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("1. 发送通知")

    override fun onRecyclerClick(position: Int, string: String) {
        sendNotification()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // IMPORTANCE_HIGH: 弹出悬浮通知 + 提示音，适合即时消息
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        )
        channel.setShowBadge(true)
        channel.description = CHANNEL_DESCRIPTION
        notificationManager?.createNotificationChannel(channel)
    }

    private fun sendNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.shared_ic_launcher)
            .setContentTitle("测试通知")
            .setContentText("这是一条测试通知")
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(++notificationId, notification)
        appendLog("发送通知成功（ID: $notificationId）")
    }

    companion object {
        private const val CHANNEL_ID = "demo_channel"
        private const val CHANNEL_NAME = "示例渠道"
        private const val CHANNEL_DESCRIPTION = "用于演示通知发送"
    }
}
