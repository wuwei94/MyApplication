package com.example.william.my.module.widget_thirdparty.hilt

import android.app.Application
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.module.widget_thirdparty.loadsir.DefaultCallback
import com.example.william.my.module.widget_thirdparty.loadsir.ErrorCallback
import com.kingja.loadsir.core.LoadSir
import javax.inject.Inject

class WidgetThirdpartyInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        initLoadSir()
    }

    override fun getApp(): Application {
        return mApp
    }

    private fun initLoadSir() {
        LoadSir.beginBuilder()
            .addCallback(ErrorCallback())
            .addCallback(DefaultCallback())
            .setDefaultCallback(DefaultCallback::class.java) //设置默认状态页
            .commit()
    }
}
