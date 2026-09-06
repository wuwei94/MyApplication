package com.example.william.my.core.base.arch.mvp

/**
 * 基于 RxJava 的 MVP Presenter 基础接口（管理订阅生命周期）
 */
interface IBaseRxJavaPresenter {
    fun subscribe() // 开启订阅
    fun unsubscribe() // 结束订阅
}
