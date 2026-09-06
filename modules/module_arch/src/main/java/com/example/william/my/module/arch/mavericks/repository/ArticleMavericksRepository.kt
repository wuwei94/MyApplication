package com.example.william.my.module.arch.mavericks.repository

import com.airbnb.mvrx.BuildConfig
import com.airbnb.mvrx.ExperimentalMavericksApi
import com.airbnb.mvrx.MavericksRepository
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.module.arch.mavericks.data.ArticleMavericksState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Mavericks 文章数据仓库
 *
 * 演示 Mavericks 体系中基于 [MavericksRepository] 进行跨页面共享状态管理的实现方式。
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
