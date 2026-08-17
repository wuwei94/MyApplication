package com.example.william.my.module.arch.mavericks.article

import com.airbnb.mvrx.ExperimentalMavericksApi
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.source.ArticleRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMavericksApi::class)
class ArticleMavericksViewModel(
    initialState: ArticleMavericksState,
    articleRepository: ArticleRepository<ArticleData, ArticleDetailData>
) : MavericksViewModel<ArticleMavericksState>(initialState) {

    private val repository = ArticleMavericksRepository(viewModelScope, articleRepository)

    init {
        viewModelScope.launch {
            repository.stateFlow.collect { repoState ->
                setState {
                    copy(articleResponse = repoState.articleResponse)
                }
            }
        }
    }

    fun loadArticle(page: Int) {
        withState {
            if (it.articleResponse is Loading) return@withState
            repository.getArticle(page)
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