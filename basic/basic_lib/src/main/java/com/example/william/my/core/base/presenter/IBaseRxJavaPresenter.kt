package com.example.william.my.core.base.presenter

interface IBaseRxJavaPresenter {
    fun subscribe() //开启订阅
    fun unsubscribe() //结束订阅
}