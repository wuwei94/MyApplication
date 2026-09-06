package com.example.william.my.module.arch.mvi.data

/**
 * MVI 用户意图（Intent）
 */
sealed class ArticleIntent {
    class LoadArticleIntent(val page: Int) : ArticleIntent()
}
