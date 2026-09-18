package com.example.william.my.core.base.timezone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.util.TimeZone

/**
 * 基于系统时区广播的响应式时区监听实现
 *
 * 核心设计：
 * 1. 【生命周期与泄漏安全】：[callbackFlow] + [awaitClose] 反注册 [BroadcastReceiver]，订阅取消即释放；
 * 2. 【广播源】：监听 [Intent.ACTION_TIMEZONE_CHANGED]（含用户改时区与夏令时切换）；
 * 3. 【零延迟初始值】：流启动瞬间发射当前 [TimeZone.getDefault]，避免广播到达前空白；
 * 4. 【背压】：[conflate] + [distinctUntilChanged]，连续相同值不重复推流。
 *
 * 与 [com.example.william.my.core.base.network.NetworkMonitor] 同属 Environment Monitors 平行对。
 *
 * 官方参考：
 * https://developer.android.google.cn/reference/android/content/Intent#ACTION_TIMEZONE_CHANGED
 */
class LiveTimeZoneMonitor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TimeZoneMonitor {

    override val currentTimeZone: Flow<TimeZone> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_TIMEZONE_CHANGED) {
                    channel.trySend(TimeZone.getDefault())
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
        channel.trySend(TimeZone.getDefault())

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
        .flowOn(ioDispatcher)
        .conflate()
        .distinctUntilChanged()
}
