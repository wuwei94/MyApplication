package com.example.william.my.module.arch.mvi.usecase

import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.base.arch.coroutine.FlowUseCase
import com.example.william.my.core.retrofit.response.RetrofitResponse
import kotlinx.coroutines.flow.Flow

/**
 * 文章业务用例（Flow 版）
 *
 * 基于 Kotlin Flow 封装文章列表请求业务用例：页码经 invoke 随调用传入，
 * 返回的冷流由 ViewModel 在收集作用域内消费，作用域取消即自动终止。
 */
class ArticleFlowUseCase(
    private val repository: ArticleRepository,
) : FlowUseCase<Int, RetrofitResponse<ArticleData>>() {

    override fun buildUseCaseFlow(parameters: Int): Flow<RetrofitResponse<ArticleData>> = repository.getArticleFlow(parameters)
}
