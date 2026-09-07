package com.example.william.my.core.base.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 单元测试专用网络状态监听替身（Test Double）
 *
 * 避免在测试中使用昂贵或脆弱的 Mock 框架，通过 [setConnected] 确定性驱动测试用例中的网络状态变迁。
 */
class TestNetworkMonitor : NetworkMonitor {

    private val connectivityFlow = MutableStateFlow(true)

    override val isOnline: Flow<Boolean> = connectivityFlow

    /**
     * 手动更新网络状态（供单测用例显式触发离线与在线自愈场景）
     */
    fun setConnected(isConnected: Boolean) {
        connectivityFlow.value = isConnected
    }
}
