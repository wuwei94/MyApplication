package com.example.william.my.core.base.arch.mvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter

/**
 * 携带 Android Application 的 ViewModel 基类
 *
 * 构造时自动完成 ARouter 依赖注入，并暴露统一的 [error] 错误状态。
 */
abstract class BaseAndroidViewModel(app: Application) :
    AndroidViewModel(app),
    DefaultLifecycleObserver {

    var error: MutableLiveData<Throwable> = MutableLiveData()

    init {
        ARouter.getInstance().inject(this)
    }
}
