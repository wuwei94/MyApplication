package com.example.william.my.module.arch.ssot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
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
 * 2. 【网络写同步】：网络同步仅执行拉取并保存到 Room，不直接传递给 UI，数据变动全由 Room InvalidationTracker 触发推流；
 * 3. 【离线高可用】：进入页面即刻从 Room 发射历史缓存，零延迟秒开展示，无网络时依然完整可用。
 */
class OfflineFirstViewModel(
    private val repository: ArticleRepository,
) : ViewModel() {

    private val isSyncingFlow = MutableStateFlow(false)
    private val lastSyncTimeFlow = MutableStateFlow<Long?>(null)

    private val _effect = Channel<OfflineFirstUiEffect>(Channel.BUFFERED)
    val effect: Flow<OfflineFirstUiEffect> = _effect.receiveAsFlow()

    /**
     * UI 状态：将 Room 数据库流与网络同步状态合并为单一不可变 UIState
     */
    val uiState: StateFlow<OfflineFirstUiState> = combine(
        repository.getArticlesStream(),
        repository.getArticleCountStream(),
        isSyncingFlow,
        lastSyncTimeFlow,
    ) { articles, count, isSyncing, lastSyncTime ->
        OfflineFirstUiState(
            articles = articles,
            cacheCount = count,
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OfflineFirstUiState(),
    )

    fun sendIntent(intent: OfflineFirstIntent) {
        viewModelScope.launch {
            when (intent) {
                is OfflineFirstIntent.Sync -> syncArticles(intent.page)
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
                    ServiceLocator.provideArticleRepository(application),
                )
            }
        }
    }
}
