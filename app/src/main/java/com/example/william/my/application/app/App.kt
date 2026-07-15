package com.example.william.my.application.app

import com.example.william.my.basic.basic_lib.MyLibEventBusIndex
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.app.BaseApp
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.core.base.utils.CrashUtils
import com.example.william.my.core.base.utils.FileSDCardUtil
import com.example.william.my.module.arch.app.ArchApp
import com.example.william.my.module.eventbus.app.EventApp
import com.example.william.my.module.flutter.app.FlutterApp
import com.example.william.my.module.opensource.app.OpenSourceApp
import com.example.william.my.modules.module_event.MyEventEventBusIndex

/**
 * gradlew :app:dependencies 查询app依赖
 */
class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()

        initCrash()

        initEventBus()
    }

    override fun initApp() {

        registerAppInit(ArchApp::class.java)
        registerAppInit(EventApp::class.java)
        registerAppInit(OpenSourceApp::class.java)

        registerAppInit(FlutterApp::class.java) // FlutterEngine
    }

    private fun initCrash() {
        CrashUtils.init(
            this, FileSDCardUtil.getCacheDirPath(this.applicationContext),
            object : CrashUtils.OnCrashListener {
                override fun onCrash(crashInfo: CrashUtils.CrashInfo) {
                    Utils.logcat("CrashUtils", crashInfo.throwable.message.toString())
                }
            })
    }

    private fun initEventBus() {
        EventBusHelper
            .addIndex(MyLibEventBusIndex())
            .addIndex(MyEventEventBusIndex())
            .init()
    }
}