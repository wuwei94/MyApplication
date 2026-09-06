package com.example.william.my.core.base.arch.mvvm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alibaba.android.arouter.launcher.ARouter

/**
 * ViewModel 基类
 *
 * 构造时自动完成 ARouter 依赖注入，并暴露统一的 [error] 错误状态。
 */
abstract class BaseViewModel :
    ViewModel(),
    DefaultLifecycleObserver {

    var error: MutableLiveData<Throwable> = MutableLiveData()

    init {
        ARouter.getInstance().inject(this)
    }
}
