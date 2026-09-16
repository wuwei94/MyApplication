package com.example.william.my.module.arch.mvi.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * MVI 页面渲染状态（UiState）
 */
sealed class ArticleUiState {
    /**
     * 加载中状态
     */
    object Loading : ArticleUiState()

    /**
     * 加载成功状态
     */
    data class Success(val articles: List<ArticleDetailData>) : ArticleUiState()

    /**
     * 加载失败状态
     */
    data class Error(val error: String?) : ArticleUiState()
}
