package com.example.william.my.module.arch.mavericks.article

import com.airbnb.mvrx.BuildConfig
import com.airbnb.mvrx.ExperimentalMavericksApi
import com.airbnb.mvrx.MavericksRepository
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Mavericks 架构文章数据仓库。
 *
 * 继承自 Mavericks 框架的 [MavericksRepository]，包装数据层 [ArticleRepository]
 * 并维护 Mavericks 不可变状态 [ArticleMavericksState]。
 */
@OptIn(ExperimentalMavericksApi::class)
class ArticleMavericksRepository(
    scope: CoroutineScope,
    private val repository: ArticleRepository,
) : MavericksRepository<ArticleMavericksState>(
    initialState = ArticleMavericksState(),
    coroutineScope = scope,
    performCorrectnessValidations = BuildConfig.DEBUG,
) {

    fun getArticle(page: Int) {
        suspend {
            repository.getArticleSuspend(page)
        }.execute(Dispatchers.IO, retainValue = ArticleMavericksState::articleResponse) {
            copy(articleResponse = it)
        }
    }
}