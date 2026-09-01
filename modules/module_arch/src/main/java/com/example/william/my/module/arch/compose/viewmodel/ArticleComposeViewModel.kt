package com.example.william.my.module.arch.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.compose.data.ArticleComposeIntent
import com.example.william.my.module.arch.compose.data.ArticleComposeState
import com.example.william.my.module.arch.compose.data.ArticleComposeUiEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Compose MVI 架构 ViewModel
 *
 * 演示现代 Compose 下的 MVI 单向数据流架构：
 * 1. Intent：通过 Channel 接收用户意图（下拉刷新 / 上拉加载更多）
 * 2. State：通过 StateFlow 暴露不可变的单向 UI 状态（列表数据、分页索引及加载中状态）
 * 3. Effect：通过 Channel 分发单次副作用事件（下拉刷新/加载更多完成通知、网络异常 Toast）
 */
class ArticleComposeViewModel(private val repository: ArticleRepository) : ViewModel() {

    val intent = Channel<ArticleComposeIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow(ArticleComposeState())
    val state: StateFlow<ArticleComposeState> = _state.asStateFlow()

    private val _effect = Channel<ArticleComposeUiEffect>(Channel.BUFFERED)
    val effect: Flow<ArticleComposeUiEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            intent.consumeAsFlow().collect { action ->
                when (action) {
                    is ArticleComposeIntent.Refresh -> loadArticles(isRefresh = true)
                    is ArticleComposeIntent.LoadMore -> loadArticles(isRefresh = false)
                }
            }
        }
    }

    private fun loadArticles(isRefresh: Boolean) {
        if (isRefresh) {
            loadJob?.cancel()
        } else if (loadJob?.isActive == true) {
            // 正在加载中，避免并发重复触发加载更多
            return
        }

        val targetPage = if (isRefresh) 0 else _state.value.page + 1
        loadJob = viewModelScope.launch {
            repository.getArticleFlow(targetPage).collect { response ->
                when {
                    response.code == RetrofitResponse.LOADING -> {
                        _state.update { current ->
                            current.copy(
                                isRefreshing = isRefresh,
                                isLoadingMore = !isRefresh
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
                                isRefreshing = false,
                                isLoadingMore = false
                            )
                        }
                        if (isRefresh) {
                            _effect.send(ArticleComposeUiEffect.RefreshComplete(isSuccess = true))
                        } else {
                            _effect.send(ArticleComposeUiEffect.LoadMoreComplete(isSuccess = true))
                        }
                    }

                    else -> {
                        val message = response.message.ifEmpty { "网络请求失败" }
                        _state.update { current ->
                            current.copy(
                                isRefreshing = false,
                                isLoadingMore = false
                            )
                        }
                        if (isRefresh) {
                            _effect.send(ArticleComposeUiEffect.RefreshComplete(isSuccess = false))
                        } else {
                            _effect.send(ArticleComposeUiEffect.LoadMoreComplete(isSuccess = false))
                        }
                        _effect.send(ArticleComposeUiEffect.ShowToast(message))
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                ArticleComposeViewModel(
                    ServiceLocator.provideArticleRepository(application)
                )
            }
        }
    }
}
