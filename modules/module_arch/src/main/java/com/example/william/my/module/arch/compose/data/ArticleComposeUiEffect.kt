package com.example.william.my.module.arch.compose.data

/**
 * Compose MVI 单次副作用事件（UiEffect）
 */
sealed class ArticleComposeUiEffect {
    /**
     * 刷新完成事件
     */
    data class RefreshComplete(val isSuccess: Boolean) : ArticleComposeUiEffect()

    /**
     * 加载更多完成事件
     */
    data class LoadMoreComplete(val isSuccess: Boolean) : ArticleComposeUiEffect()

    /**
     * 展示 Toast 事件
     */
    data class ShowToast(val message: String) : ArticleComposeUiEffect()
}
