package com.example.william.my.module.arch.compose.data

/**
 * Compose MVI 用户意图（Intent）
 */
sealed class ArticleComposeIntent {
    data object Refresh : ArticleComposeIntent()
    data object LoadMore : ArticleComposeIntent()
}
