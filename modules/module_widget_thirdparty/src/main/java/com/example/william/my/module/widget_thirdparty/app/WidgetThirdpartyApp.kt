package com.example.william.my.module.widget_thirdparty.app

import com.example.william.my.core.base.app.BaseAppInit
import com.example.william.my.module.widget_thirdparty.loadsir.DefaultCallback
import com.example.william.my.module.widget_thirdparty.loadsir.ErrorCallback
import com.kingja.loadsir.core.LoadSir

class WidgetThirdpartyApp : BaseAppInit() {

    override fun init() {
        super.init()

        initLoadSir()
    }

    private fun initLoadSir() {
        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(DefaultCallback())
            .setDefaultCallback(DefaultCallback::class.java) //设置默认状态页
            .commit()
    }
}
