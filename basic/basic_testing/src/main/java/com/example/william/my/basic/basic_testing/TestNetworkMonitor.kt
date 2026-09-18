package com.example.william.my.basic.basic_testing

import com.example.william.my.core.base.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 网络状态监听测试替身（Test Double）
 *
 * 通过 [setConnected] 确定性驱动离线 / 在线场景，供 ViewModel 与同步链路单测使用。
 */
class TestNetworkMonitor(
    initiallyOnline: Boolean = true,
) : NetworkMonitor {

    private val connectivityFlow = MutableStateFlow(initiallyOnline)

    override val isOnline: Flow<Boolean> = connectivityFlow.asStateFlow()

    /**
     * 手动更新网络状态（供单测显式触发离线与在线自愈场景）
     */
    fun setConnected(isConnected: Boolean) {
        connectivityFlow.value = isConnected
    }
}
