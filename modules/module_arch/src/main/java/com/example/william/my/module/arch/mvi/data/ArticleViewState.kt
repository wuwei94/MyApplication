package com.example.william.my.module.arch.mvi.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * MVI 页面渲染状态（ViewState）
 */
sealed class ArticleViewState {
    object Loading : ArticleViewState()
    data class Success(val articles: List<ArticleDetailData>) : ArticleViewState()
    data class Error(val error: String?) : ArticleViewState()
}
