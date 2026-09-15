package com.example.william.my.module.arch.fake

import com.example.william.my.basic.basic_repo.sync.SyncManager
import com.example.william.my.core.base.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 响应式网络监听测试替身
 */
class TestNetworkMonitor : NetworkMonitor {
    private val isOnlineFlow = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = isOnlineFlow.asStateFlow()

    fun setConnected(isConnected: Boolean) {
        isOnlineFlow.value = isConnected
    }
}

/**
 * 增量数据同步门面测试替身
 */
class TestSyncManager : SyncManager {
    private val isSyncingFlow = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = isSyncingFlow.asStateFlow()

    var syncRequestedCount: Int = 0
        private set

    override fun requestSync() {
        syncRequestedCount++
    }

    fun setSyncing(isSyncing: Boolean) {
        isSyncingFlow.value = isSyncing
    }
}
