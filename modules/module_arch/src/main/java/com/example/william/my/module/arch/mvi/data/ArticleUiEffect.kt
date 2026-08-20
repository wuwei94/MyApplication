package com.example.william.my.module.arch.mvi.data

/**
 * MVI 单次副作用事件（UiEffect）
 */
sealed class ArticleUiEffect {
    data class ShowToast(val message: String) : ArticleUiEffect()
}
