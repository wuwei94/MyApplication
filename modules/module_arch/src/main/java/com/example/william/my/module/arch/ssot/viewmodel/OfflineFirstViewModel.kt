package com.example.william.my.module.arch.ssot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_datastore.SyncPreferencesDataSource
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.basic.basic_repo.sync.SyncManager
import com.example.william.my.basic.basic_sync.Sync
import com.example.william.my.core.base.network.NetworkMonitor
import com.example.william.my.module.arch.ssot.data.OfflineFirstIntent
import com.example.william.my.module.arch.ssot.data.OfflineFirstUiEffect
import com.example.william.my.module.arch.ssot.data.OfflineFirstUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 离线优先（Offline-First / SSOT）ViewModel
 *
 * 核心架构体现：
 * 1. 【唯一真实来源（SSOT）】：UI 所观察的 [uiState] 中，文章列表 articles 与数量 cacheCount 完全由
 *    Room 本地数据库 Flow 驱动，ViewModel 内部不维护、也无法手动修改 articles 集合；
 * 2. 【响应式网络监听与在线自愈】：结合 [NetworkMonitor] 观察网络状态，断网时通知 UI 展现离线横幅，连网瞬间自动触发增量同步；
 * 3. 【后台增量同步】：通过 [SyncManager] + [SyncPreferencesDataSource] 维护版本游标，配合 WorkManager 实现幂等增量拉取；
 * 4. 【离线高可用】：进入页面即刻从 Room 发射历史缓存，零延迟秒开展示，无网络时依然完整可用。
 */
class OfflineFirstViewModel(
    private val repository: ArticleRepository,
    private val networkMonitor: NetworkMonitor,
    private val syncManager: SyncManager,
    private val syncPreferences: SyncPreferencesDataSource,
) : ViewModel() {

    private val isSyncingFlow = MutableStateFlow(false)
    private val lastSyncTimeFlow = MutableStateFlow<Long?>(null)

    private val _effect = Channel<OfflineFirstUiEffect>(Channel.BUFFERED)
    val effect: Flow<OfflineFirstUiEffect> = _effect.receiveAsFlow()

    private data class SyncStatusSnapshot(
        val isSyncing: Boolean,
        val lastSyncTime: Long?,
        val isOnline: Boolean,
        val articleVersion: Int,
        val isWorkManagerSyncing: Boolean,
    )

    private val syncStatusFlow = combine(
        isSyncingFlow,
        lastSyncTimeFlow,
        networkMonitor.isOnline,
        syncPreferences.changeListVersions,
        syncManager.isSyncing,
    ) { isSyncing, lastSyncTime, isOnline, versions, isWorkManagerSyncing ->
        SyncStatusSnapshot(
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
            isOnline = isOnline,
            articleVersion = versions.articleVersion,
            isWorkManagerSyncing = isWorkManagerSyncing,
        )
    }

    /**
     * UI 状态：将 Room 数据库流与网络监听、同步游标及状态合并为单一不可变 UIState
     */
    val uiState: StateFlow<OfflineFirstUiState> = combine(
        repository.getArticlesStream(),
        repository.getArticleCountStream(),
        syncStatusFlow,
    ) { articles, count, status ->
        OfflineFirstUiState(
            articles = articles,
            cacheCount = count,
            isSyncing = status.isSyncing,
            lastSyncTime = status.lastSyncTime,
            isOnline = status.isOnline,
            articleVersion = status.articleVersion,
            isWorkManagerSyncing = status.isWorkManagerSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OfflineFirstUiState(),
    )

    init {
        // 在线自愈机制（Auto-Healing）：当检测到网络从断开恢复为在线时，自动发起增量拉取
        viewModelScope.launch {
            var wasConnected: Boolean? = null
            networkMonitor.isOnline.collect { isConnected ->
                if (wasConnected == false && isConnected) {
                    _effect.send(OfflineFirstUiEffect.ShowToast("检测到网络已恢复，正在自动执行在线自愈增量同步..."))
                    syncManager.requestSync()
                }
                wasConnected = isConnected
            }
        }
    }

    fun sendIntent(intent: OfflineFirstIntent) {
        viewModelScope.launch {
            when (intent) {
                is OfflineFirstIntent.Sync -> syncArticles(intent.page)
                is OfflineFirstIntent.TriggerWorkManagerSync -> {
                    syncManager.requestSync()
                    _effect.send(OfflineFirstUiEffect.ShowToast("已提交 WorkManager 后台增量同步任务（带连网约束）"))
                }
                is OfflineFirstIntent.SimulateRemoteNewVersion -> {
                    repository.simulateRemoteNewVersion(intent.title)
                    _effect.send(OfflineFirstUiEffect.ShowToast("远端已产生新版本变更，可点击同步验证增量拉取"))
                }
                is OfflineFirstIntent.AddLocalArticle -> addLocalArticle(intent.title)
                is OfflineFirstIntent.ClearLocalCache -> clearLocalCache()
            }
        }
    }

    private suspend fun syncArticles(page: Int) {
        if (isSyncingFlow.value) return
        isSyncingFlow.value = true
        val result = repository.syncArticles(page)
        isSyncingFlow.value = false
        if (result.isSuccess) {
            lastSyncTimeFlow.value = System.currentTimeMillis()
            _effect.send(OfflineFirstUiEffect.SyncComplete(isSuccess = true))
            _effect.send(OfflineFirstUiEffect.ShowToast("网络同步成功，已写入 Room 数据库并自动推流"))
        } else {
            _effect.send(OfflineFirstUiEffect.SyncComplete(isSuccess = false))
            val errorMsg = result.exceptionOrNull()?.message ?: "网络请求异常"
            _effect.send(OfflineFirstUiEffect.ShowToast("网络同步失败: $errorMsg（界面仍稳定展示 Room 离线缓存）"))
        }
    }

    private suspend fun addLocalArticle(title: String) {
        repository.insertLocalArticle(title)
        _effect.send(OfflineFirstUiEffect.ShowToast("已直接向 Room 插入一条记录，UI 自动感知更新"))
    }

    private suspend fun clearLocalCache() {
        repository.clearLocalArticles()
        _effect.send(OfflineFirstUiEffect.ShowToast("已清空 Room 数据库，UI 自动清空"))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY],
                )
                OfflineFirstViewModel(
                    repository = ServiceLocator.provideArticleRepository(application),
                    networkMonitor = ServiceLocator.provideNetworkMonitor(application),
                    syncManager = Sync.provideSyncManager(application),
                    syncPreferences = Sync.provideSyncPreferencesDataSource(application),
                )
            }
        }
    }
}
