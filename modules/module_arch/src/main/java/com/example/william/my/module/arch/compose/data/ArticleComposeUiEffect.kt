package com.example.william.my.module.arch.compose.data

/**
 * Compose MVI 单次副作用事件（UiEffect）
 */
sealed class ArticleComposeUiEffect {
    data class RefreshComplete(val isSuccess: Boolean) : ArticleComposeUiEffect()
    data class LoadMoreComplete(val isSuccess: Boolean) : ArticleComposeUiEffect()
    data class ShowToast(val message: String) : ArticleComposeUiEffect()
}
