package com.example.william.my.core.base.app.hilt.interfaces

import android.app.Application
import android.content.res.Configuration

/**
 * 应用初始化接口
 *
 * 定义应用级初始化的统一入口，由各模块实现，经 Hilt 注入后由框架按生命周期回调。
 */
interface IAppInit {

    fun init(app: Application)

    fun initAsync(app: Application) {}

    fun onLowMemory() {}

    fun onConfigurationChanged(newConfig: Configuration) {}

    fun getApp(): Application
}
