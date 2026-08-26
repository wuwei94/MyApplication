package com.example.william.my.module.arch.mavericks.viewmodel

import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.module.arch.mavericks.data.ArticleMavericksState
import kotlinx.coroutines.Dispatchers

/**
 * Mavericks 文章列表 ViewModel
 *
 * 管理文章列表状态并驱动 UI 更新，演示 Mavericks 的异步请求与状态变更。
 */
class ArticleMavericksViewModel(
    initialState: ArticleMavericksState,
    private val articleRepository: ArticleRepository
) : MavericksViewModel<ArticleMavericksState>(initialState) {

    fun loadArticle(page: Int) {
        withState { state ->
            if (state.articleResponse is Loading) return@withState
            suspend {
                articleRepository.getArticleSuspend(page)
            }.execute(Dispatchers.IO) {
                copy(articleResponse = it)
            }
        }
    }

    /**
     * Mavericks 框架工厂约定：
     * 当 ViewModel 包含除 initialState 以外的依赖（如 ArticleRepository）时，
     * Mavericks 框架在运行时通过反射查找 Companion 对象实现的 MavericksViewModelFactory 进行实例化。
     */
    companion object : MavericksViewModelFactory<ArticleMavericksViewModel, ArticleMavericksState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ArticleMavericksState
        ): ArticleMavericksViewModel {
            val articleRepo =
                ServiceLocator.provideArticleRepository(viewModelContext.activity.applicationContext)
            return ArticleMavericksViewModel(state, articleRepo)
        }
    }
}
