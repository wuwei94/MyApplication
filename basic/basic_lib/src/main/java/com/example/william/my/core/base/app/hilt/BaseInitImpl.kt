package com.example.william.my.core.base.app.hilt

import android.app.Application
import com.example.william.my.core.base.app.BaseApp
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import javax.inject.Inject

/**
 * 应用初始化默认实现
 *
 * 负责保存全局 [Application] 等基础能力，作为 [IAppInit] 的默认注入实例。
 */
class BaseInitImpl @Inject constructor() : IAppInit {

    private lateinit var app: Application

    override fun init(app: Application) {
        this.app = app
        BaseApp.setApp(app)
    }

    override fun getApp(): Application = app
}
