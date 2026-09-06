package com.example.william.my.module.arch.ssot.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * 离线优先（Offline-First / SSOT）UI 状态
 *
 * @param articles 永远只来自 Room 数据库的数据流（SSOT 唯一来源）
 * @param cacheCount 本地缓存条数（由 Room COUNT 查询流实时驱动）
 * @param isSyncing 网络写同步进行状态（指示当前是否正在从网络拉取写入 DB）
 * @param lastSyncTime 上次成功完成网络同步的时间戳
 */
data class OfflineFirstUiState(
    val articles: List<ArticleDetailData> = emptyList(),
    val cacheCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
)

/**
 * 用户交互意图（Intent）
 */
sealed interface OfflineFirstIntent {

    /**
     * 触发网络同步（拉取远端数据写入 Room 本地数据库，不直传 UI）
     */
    data class Sync(val page: Int = 0) : OfflineFirstIntent

    /**
     * 模拟本地插入（绕过网络直接向 Room 写入测试文章，验证 UI 是否自动响应）
     */
    data class AddLocalArticle(val title: String) : OfflineFirstIntent

    /**
     * 清空本地缓存（直接清空 Room，验证 UI 是否随底层清空而变为空视图）
     */
    data object ClearLocalCache : OfflineFirstIntent
}

/**
 * 单次副作用事件（Effect）
 */
sealed interface OfflineFirstUiEffect {

    /**
     * 展示 Toast 事件
     */
    data class ShowToast(val message: String) : OfflineFirstUiEffect

    /**
     * 同步完成事件
     */
    data class SyncComplete(val isSuccess: Boolean) : OfflineFirstUiEffect
}
