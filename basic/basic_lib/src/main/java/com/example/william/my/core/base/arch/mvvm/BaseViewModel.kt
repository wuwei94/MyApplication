package com.example.william.my.core.base.arch.mvvm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel 基类
 *
 * 暴露统一的 [error] 错误状态。
 */
abstract class BaseViewModel :
    ViewModel(),
    DefaultLifecycleObserver {

    var error: MutableLiveData<Throwable> = MutableLiveData()
}
