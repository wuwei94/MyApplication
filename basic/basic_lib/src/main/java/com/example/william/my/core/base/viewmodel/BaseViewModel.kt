package com.example.william.my.core.base.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alibaba.android.arouter.launcher.ARouter

abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {

    var error: MutableLiveData<Throwable> = MutableLiveData()

    init {
        ARouter.getInstance().inject(this)
    }
}
