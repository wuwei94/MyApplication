package com.example.william.my.module.arch.mavericks.viewmodel

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.module.arch.mavericks.data.ArticleMavericksState
import com.example.william.my.module.arch.mavericks.repository.ArticleMavericksRepository
import kotlinx.coroutines.launch

/**
 * Mavericks 文章列表 ViewModel
 *
 * 管理文章列表状态并驱动 UI 更新，演示基于 [ArticleMavericksRepository]（继承自 MavericksRepository）的响应式架构。
 */
class ArticleMavericksViewModel(
    initialState: ArticleMavericksState,
    private val mavericksRepository: ArticleMavericksRepository,
) : MavericksViewModel<ArticleMavericksState>(initialState) {

    init {
        // 订阅 MavericksRepository 的状态流并同步到当前 ViewModel 的 State
        viewModelScope.launch {
            mavericksRepository.stateFlow.collect { repoState ->
                setState { copy(articleResponse = repoState.articleResponse) }
            }
        }
    }

    fun loadArticle(page: Int) {
        withState { state ->
            if (state.articleResponse is Loading) return@withState
            mavericksRepository.getArticle(page)
        }
    }

    /**
     * Mavericks 框架工厂约定：
     * 当 ViewModel 包含除 initialState 以外的依赖（如 ArticleMavericksRepository）时，
     * Mavericks 框架在运行时通过反射查找 Companion 对象实现的 MavericksViewModelFactory 进行实例化。
     */
    companion object : MavericksViewModelFactory<ArticleMavericksViewModel, ArticleMavericksState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ArticleMavericksState,
        ): ArticleMavericksViewModel {
            val articleRepo =
                ServiceLocator.provideArticleRepository(viewModelContext.activity.applicationContext)
            val mavericksRepo = ArticleMavericksRepository(
                scope = viewModelContext.activity.lifecycleScope,
                repository = articleRepo,
            )
            return ArticleMavericksViewModel(state, mavericksRepo)
        }
    }
}
