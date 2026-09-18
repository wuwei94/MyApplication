package com.example.william.my.core.base.timezone

import kotlinx.coroutines.flow.Flow
import java.util.TimeZone

/**
 * 响应式系统时区监听契约
 *
 * 对齐 Google Now in Android Environment Monitors：
 * 系统时区切换或夏令时变更时向上层推送最新时区，
 * 供时间格式化 UI 与离线时间戳计算保持一致，避免 App 不重启时的时区漂移。
 */
interface TimeZoneMonitor {

    /**
     * 当前系统时区的响应式冷流。
     *
     * - 初始发射：订阅瞬间的 `TimeZone.getDefault()`；
     * - 后续发射：`ACTION_TIMEZONE_CHANGED` 广播到达后的最新系统时区。
     */
    val currentTimeZone: Flow<TimeZone>
}
