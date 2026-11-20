package com.example.william.my.core.base.arch.mvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData

/**
 * 携带 Android Application 的 ViewModel 基类
 *
 * 暴露统一的 [error] 错误状态。
 */
abstract class BaseAndroidViewModel(app: Application) :
    AndroidViewModel(app),
    DefaultLifecycleObserver {

    var error: MutableLiveData<Throwable> = MutableLiveData()
}
