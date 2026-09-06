package com.example.william.my.module.arch.mvi.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * MVI 页面渲染状态（ViewState）
 */
sealed class ArticleViewState {
    /**
     * 加载中状态
     */
    object Loading : ArticleViewState()

    /**
     * 加载成功状态
     */
    data class Success(val articles: List<ArticleDetailData>) : ArticleViewState()

    /**
     * 加载失败状态
     */
    data class Error(val error: String?) : ArticleViewState()
}
