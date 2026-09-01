package com.example.william.my.module.arch.compose.data

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * Compose MVI 页面渲染状态（ViewState）
 *
 * @param articles 文章列表数据
 * @param isRefreshing 是否正在下拉刷新
 * @param isLoadingMore 是否正在上拉加载更多
 * @param page 当前分页页码（从 0 开始）
 */
data class ArticleComposeState(
    val articles: List<ArticleDetailData> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val page: Int = 0
)
