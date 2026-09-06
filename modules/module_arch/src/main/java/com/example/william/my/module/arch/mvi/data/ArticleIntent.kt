package com.example.william.my.module.arch.mvi.data

/**
 * MVI 用户意图（Intent）
 */
sealed interface ArticleIntent {
    data class LoadArticleIntent(val page: Int) : ArticleIntent
    data object Refresh : ArticleIntent
    data object LoadMore : ArticleIntent
}
