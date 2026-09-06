package com.example.william.my.application.app

import com.blankj.utilcode.util.CrashUtils
import com.example.william.my.basic.basic_lib.MyLibEventBusIndex
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.app.BaseApp
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.module.arch.app.MavericksApp
import com.example.william.my.module.event.app.EventApp
import com.example.william.my.module.flutter.app.FlutterApp
import com.example.william.my.module.widget_thirdparty.app.LoadSirApp
import com.example.william.my.modules.module_event.MyEventEventBusIndex

/**
 * 应用入口 Application。
 *
 * 负责注册各模块的 AppInit 初始化逻辑，并在启动时初始化崩溃捕获与 EventBus。
 *
 * 提示：gradlew :app:dependencies 可查询 app 依赖。
 */
class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()

        initCrash()

        initEventBus()
    }

    override fun initApp() {
        registerAppInit(EventApp::class.java)
        registerAppInit(MavericksApp::class.java)

        registerAppInit(LoadSirApp::class.java)

        registerAppInit(FlutterApp::class.java) // FlutterEngine
    }

    private fun initCrash() {
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
