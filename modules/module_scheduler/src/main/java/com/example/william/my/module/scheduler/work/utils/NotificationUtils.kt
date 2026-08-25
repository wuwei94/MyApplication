@file:JvmName("NotificationUtils")

package com.example.william.my.module.scheduler.work.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.example.william.my.module.scheduler.R
import java.util.UUID

/**
 * 为加急后台任务 (Expedited Work) 创建前台通知和通知渠道 (O+)
 */
fun createNotification(
    context: Context,
    workRequestId: UUID,
    notificationTitle: String
): Notification {
    val channelId = context.getString(R.string.scheduler_notification_channel_id)
    val channelName = context.getString(R.string.scheduler_notification_channel_name)
    val cancelText = context.getString(R.string.scheduler_notification_cancel_processing)
    // 用于取消正在运行的任务的 PendingIntent
    val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(workRequestId)

    val builder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(notificationTitle)
        .setTicker(notificationTitle)
        .setSmallIcon(R.drawable.scheduler_ic_gradient)
        .setOngoing(true)
        .addAction(android.R.drawable.ic_delete, cancelText, cancelIntent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context, channelId, channelName).also {
            builder.setChannelId(it.id)
        }
    }
    return builder.build()
}

/**
 * 为 Android 8.0 (API 26+) 创建通知渠道
 */
@TargetApi(Build.VERSION_CODES.O)
fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
    notificationImportance: Int = NotificationManager.IMPORTANCE_HIGH
): NotificationChannel {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return NotificationChannel(
        channelId, name, notificationImportance
    ).also { channel ->
        notificationManager.createNotificationChannel(channel)
    }
}
