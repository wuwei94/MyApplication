package com.example.william.my.application.app

import android.app.Application
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.AppInit
import com.example.william.my.core.base.hilt.qualifier.BaseInit
import com.example.william.my.core.base.hilt.qualifier.EventInit
import com.example.william.my.core.base.hilt.qualifier.FlutterInit
import com.example.william.my.core.base.hilt.qualifier.MavericksInit
import com.example.william.my.core.base.hilt.qualifier.OpenSourceInit
import com.example.william.my.core.base.hilt.qualifier.WidgetThirdpartyInit
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AppHilt : Application() {

    @BaseInit
    @Inject
    lateinit var baseInit: IAppInit

    @AppInit
    @Inject
    lateinit var appInit: IAppInit

    @EventInit
    @Inject
    lateinit var eventInit: IAppInit

    @WidgetThirdpartyInit
    @Inject
    lateinit var widgetThirdpartyInit: IAppInit

    @MavericksInit
    @Inject
    lateinit var mavericksInit: IAppInit

    @OpenSourceInit
    @Inject
    lateinit var openSourceInit: IAppInit

    @FlutterInit
    @Inject
    lateinit var flutterInit: IAppInit


    override fun onCreate() {
        super.onCreate()

        baseInit.init(this)

        appInit.init(this)

        eventInit.init(this)
        mavericksInit.init(this)
        widgetThirdpartyInit.init(this)
        openSourceInit.init(this)

        flutterInit.init(this) // FlutterEngine
    }
}