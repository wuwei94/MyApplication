package com.example.william.my.module.loadsir.app

import com.example.william.my.core.base.app.BaseAppInit
import com.example.william.my.module.loadsir.callback.DefaultCallback
import com.example.william.my.module.loadsir.callback.ErrorCallback
import com.kingja.loadsir.core.LoadSir

class LoadSirApp : BaseAppInit() {

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
