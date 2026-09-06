package com.example.william.my.module.arch.mvi.data

/**
 * MVI 用户意图（Intent）
 */
sealed class ArticleIntent {
    /**
     * 加载文章意图
     */
    class LoadArticleIntent(val page: Int) : ArticleIntent()
}
