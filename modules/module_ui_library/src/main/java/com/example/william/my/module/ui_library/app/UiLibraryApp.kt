package com.example.william.my.module.ui_library.app

import com.example.william.my.core.base.app.BaseAppInit
import com.example.william.my.module.ui_library.loadsir.DefaultCallback
import com.example.william.my.module.ui_library.loadsir.ErrorCallback
import com.kingja.loadsir.core.LoadSir

class UiLibraryApp : BaseAppInit() {

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
