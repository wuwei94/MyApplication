package com.example.william.my.basic.basic_repo.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 单元测试专用同步管理器替身（Test Double）
 */
class TestSyncManager : SyncManager {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = _isSyncing

    override fun requestSync() {
        _isSyncing.value = true
    }

    /**
     * 手动更新同步状态（供单测使用）
     */
    fun setSyncing(isSyncing: Boolean) {
        _isSyncing.value = isSyncing
    }
}
