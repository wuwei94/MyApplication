package com.example.william.my.basic.basic_repo.sync

import kotlinx.coroutines.flow.Flow

/**
 * 增量数据同步门面接口
 *
 * 向上层 ViewModel 与 UI 提供同步状态观测流与手动发起同步的能力。
 */
interface SyncManager {

    /**
     * 当前是否有后台同步任务正在执行中
     */
    val isSyncing: Flow<Boolean>

    /**
     * 手动请求发起一次后台增量同步
     */
    fun requestSync()
}
