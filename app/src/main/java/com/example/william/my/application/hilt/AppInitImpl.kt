package com.example.william.my.application.hilt

import android.app.Application
import com.blankj.utilcode.util.CrashUtils
import com.example.william.my.basic.basic_lib.MyLibEventBusIndex
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.modules.module_event.MyEventEventBusIndex
import javax.inject.Inject

class AppInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        initCrash(app)

        initEventBus()
    }

    override fun getApp(): Application = mApp

    private fun initCrash(app: Application) {
        CrashUtils.init { crashInfo ->
            Utils.logcat("CrashUtils", crashInfo.throwable.message.toString())
        }
    }

    private fun initEventBus() {
        EventBusHelper
            .addIndex(MyLibEventBusIndex())
            .addIndex(MyEventEventBusIndex())
            .init()
    }
}
