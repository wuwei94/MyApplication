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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 文章列表 StateFlow ViewModel
 *
 * 演示 MVI 模式中通过 Channel 接收 Intent、StateFlow 暴露 UIState，以及 Channel 分发 Effect 副作用。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleStateFlowViewModel(private val repository: ArticleRepository) :
    ViewModel() {

    val intent = Channel<ArticleIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<ArticleViewState>(ArticleViewState.Loading)
    val state: StateFlow<ArticleViewState>
        get() = _state

    private val _effect = Channel<ArticleUiEffect>(Channel.BUFFERED)
    val effect: Flow<ArticleUiEffect>
        get() = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            intent.consumeAsFlow()
                .flatMapLatest { intent ->
                    when (intent) {
                        is ArticleIntent.LoadArticleIntent -> repository.getArticleFlow(intent.page)
                    }
                }
                .collect { response ->
                    when {
                        response.code == RetrofitResponse.LOADING -> {
                            _state.value = ArticleViewState.Loading
                        }

                        response.isSuccess -> {
                            val datas = response.data?.datas ?: emptyList()
                            _state.value = ArticleViewState.Success(datas)
                        }

                        else -> {
                            val message = response.message.ifEmpty { "网络请求失败" }
                            _effect.send(ArticleUiEffect.ShowToast(message))
                            _state.value = ArticleViewState.Error(message)
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
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                ArticleStateFlowViewModel(
                    ServiceLocator.provideArticleRepository(application)
                )
            }
        }
    }
}
