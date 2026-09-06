package com.example.william.my.module.arch.compose.data

/**
 * Compose MVI 用户意图（Intent）
 */
sealed class ArticleComposeIntent {
    /**
     * 刷新意图
     */
    data object Refresh : ArticleComposeIntent()

    /**
     * 加载更多意图
     */
    data object LoadMore : ArticleComposeIntent()
}
