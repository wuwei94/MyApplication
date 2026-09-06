package com.example.william.my.module.arch.mvi.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * MVI 页面渲染状态（ViewState）
 *
 * 遵循 MVI 核心规范：ViewState 代表当前页面的完整不可变状态快照（包含累计文章列表、分页与加载状态），
 * 保证配置变更/重建时状态可完整恢复，不依赖 View 层缓存。
 */
data class ArticleViewState(
    val articles: List<ArticleDetailData> = emptyList(),
    val page: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
)
