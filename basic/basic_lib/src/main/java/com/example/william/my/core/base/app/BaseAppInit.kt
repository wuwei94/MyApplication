package com.example.william.my.core.base.app

import android.app.Application
import android.content.res.Configuration

/**
 * 模块 Application 初始化基类
 *
 * 各业务模块继承本类并通过 [BaseApp.registerAppInit] 注册，由 [BaseApp] 在应用启动阶段统一回调
 * [init]、[initAsync] 等方法，实现模块级初始化的解耦。
 */
abstract class BaseAppInit {

    lateinit var app: Application

    fun setApplication(app: Application) {
        this.app = app
    }

    open fun init() {}
    open fun initAsync() {}
    open fun onLowMemory() {}
    open fun onConfigurationChanged(newConfig: Configuration) {}
}
