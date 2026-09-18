package com.example.william.my.module.widget_thirdparty.hilt

import android.app.Application
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.module.widget_thirdparty.callback.DefaultCallback
import com.example.william.my.module.widget_thirdparty.callback.ErrorCallback
import com.kingja.loadsir.core.LoadSir
import javax.inject.Inject

/**
 * WidgetThirdparty 模块 Hilt 初始化实现（IAppInit 方案）。
 *
 * 与手动方案 [com.example.william.my.module.widget_thirdparty.app.WidgetThirdpartyApp] 一一对应，
 * 二者都配置 LoadSir 默认状态视图，仅初始化机制不同：
 *  - 手动方案：继承 BaseAppInit，由 App 在 initApp() 中 registerAppInit(...) 触发；
 *  - Hilt 方案：实现 IAppInit，由本类经 @WidgetThirdpartyInit 绑定后注入 AppHilt 触发。
 * 切换 AndroidManifest 中的 android:name（App / AppHilt）即可二选一。
 */
class WidgetThirdpartyInitImpl @Inject constructor() : IAppInit {

    private lateinit var app: Application

    override fun init(app: Application) {
        this.app = app

        initLoadSir()
    }

    override fun getApp(): Application = app

    private fun initLoadSir() {
        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(DefaultCallback())
            .setDefaultCallback(DefaultCallback::class.java) // 设置默认状态页
            .commit()
    }
}
