package com.example.william.my.module.arch.mavericks.data

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.core.retrofit.response.RetrofitResponse

/**
 * Mavericks 文章列表不可变状态
 *
 * 必须是 data class 且实现 [MavericksState] 接口，所有属性均为不可变的 val。
 */
data class ArticleMavericksState(
    val articleResponse: Async<RetrofitResponse<ArticleData>> = Uninitialized,
) : MavericksState
