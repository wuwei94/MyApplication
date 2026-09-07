package com.example.william.my.core.base.network

import kotlinx.coroutines.flow.Flow

/**
 * 响应式网络状态监听契约
 *
 * 对齐 Google Now in Android 核心数据层规范：
 * 向上层暴露响应式网络可用性流（[isOnline]），供 UI 呈现离线横幅以及数据层调度自动重试与自愈同步。
 */
interface NetworkMonitor {

    /**
     * 当前网络是否处于可用连通状态的响应式冷流。
     *
     * - `true`: 网络可用且具备互联网访问能力（NET_CAPABILITY_INTERNET）；
     * - `false`: 离线断网或网络不可用。
     */
    val isOnline: Flow<Boolean>
}
