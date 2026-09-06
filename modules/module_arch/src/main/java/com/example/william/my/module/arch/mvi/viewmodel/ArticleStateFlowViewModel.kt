package com.example.william.my.module.arch.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.mvi.data.ArticleIntent
import com.example.william.my.module.arch.mvi.data.ArticleUiEffect
import com.example.william.my.module.arch.mvi.data.ArticleViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 文章列表 StateFlow ViewModel
 *
 * 遵循严格的 MVI 单向数据流（UDF）模式：
 * 1. Intent：通过 Channel 接收意图（Refresh / LoadMore / LoadArticleIntent）
 * 2. State：通过不可变 StateFlow 暴露全量 UI 状态（维护累积文章列表，状态可完整恢复）
 * 3. Effect：通过 Channel 分发单次瞬时副作用（如 Toast 提示）
 */
class ArticleStateFlowViewModel(private val repository: ArticleRepository) : ViewModel() {

    val intent = Channel<ArticleIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow(ArticleViewState())
    val state: StateFlow<ArticleViewState> = _state.asStateFlow()

    private val _effect = Channel<ArticleUiEffect>(Channel.BUFFERED)
    val effect: Flow<ArticleUiEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            intent.receiveAsFlow().collect { action ->
                when (action) {
                    is ArticleIntent.Refresh -> loadArticles(targetPage = 0, isRefresh = true)
                    is ArticleIntent.LoadMore -> loadArticles(targetPage = _state.value.page + 1, isRefresh = false)
                    is ArticleIntent.LoadArticleIntent -> {
                        val isRefresh = action.page == 0
                        loadArticles(targetPage = action.page, isRefresh = isRefresh)
                    }
                }
            }
        }
    }

    fun sendIntent(action: ArticleIntent) {
        viewModelScope.launch {
            intent.send(action)
        }
    }

    private fun loadArticles(targetPage: Int, isRefresh: Boolean) {
        if (isRefresh) {
            loadJob?.cancel()
        } else if (loadJob?.isActive == true) {
            return
        }

        loadJob = viewModelScope.launch {
            repository.getArticleFlow(targetPage).collect { response ->
                when {
                    response.code == RetrofitResponse.LOADING -> {
                        _state.update { current ->
                            current.copy(
                                isLoading = true,
                                isRefreshing = isRefresh,
                                isLoadingMore = !isRefresh,
                            )
                        }
                    }

                    response.isSuccess -> {
                        val newArticles = response.data?.datas ?: emptyList()
                        _state.update { current ->
                            val updatedList = if (isRefresh) {
                                newArticles
                            } else {
                                current.articles + newArticles
                            }
                            current.copy(
                                articles = updatedList,
                                page = targetPage,
                                isLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                            )
                        }
                    }

                    else -> {
                        val message = response.message.ifEmpty { "网络请求失败" }
                        _state.update { current ->
                            current.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                            )
                        }
                        _effect.send(ArticleUiEffect.ShowToast(message))
                    }
                }
            }
        }
    }

    companion object {
        /**
         * 工厂：通过 [viewModelFactory] DSL 从 [CreationExtras] 获取 Application 并注入仓库
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY],
                )
                ArticleStateFlowViewModel(
                    ServiceLocator.provideArticleRepository(application),
                )
            }
        }
    }
}
