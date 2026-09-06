package com.example.william.my.module.widget_thirdparty.app

import com.example.william.my.core.base.app.BaseAppInit
import com.example.william.my.module.widget_thirdparty.callback.DefaultCallback
import com.example.william.my.module.widget_thirdparty.callback.ErrorCallback
import com.kingja.loadsir.core.LoadSir

/**
 * LoadSir 初始化
 *
 * 在应用启动时初始化 LoadSir 状态视图配置。
 */
class LoadSirApp : BaseAppInit() {

    override fun init() {
        super.init()

        initLoadSir()
    }

    private fun initLoadSir() {
        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(DefaultCallback())
            .setDefaultCallback(DefaultCallback::class.java) // 设置默认状态页
            .commit()
    }
}
