package com.example.william.my.module.component.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 前台 Service
 *
 * 必须在 5 秒内调用 startForeground() 显示通知，否则会 ANR。
 * 通知消失前服务一直运行，适合需要用户感知的长时间任务。
 */
class MyForegroundService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.shared_ic_launcher)
            .setContentTitle("前台服务")
            .setContentText("服务正在运行")
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Utils.toast("前台服务已启动")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "前台服务",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "foreground_service"
        private const val NOTIFICATION_ID = 1001
    }
}
