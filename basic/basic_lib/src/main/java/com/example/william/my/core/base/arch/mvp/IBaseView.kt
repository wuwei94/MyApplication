package com.example.william.my.core.base.arch.mvp

/**
 * MVP 架构 View 基础接口
 */
interface IBaseView<T> {
    /**
     * 显示提示信息
     */
    fun showToast(message: String?)
}
