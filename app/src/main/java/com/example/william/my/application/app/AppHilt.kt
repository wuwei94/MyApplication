package com.example.william.my.application.app

import android.app.Application
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.AppInit
import com.example.william.my.core.base.hilt.qualifier.BaseInit
import com.example.william.my.core.base.hilt.qualifier.EventInit
import com.example.william.my.core.base.hilt.qualifier.FlutterInit
import com.example.william.my.core.base.hilt.qualifier.MavericksInit
import com.example.william.my.core.base.hilt.qualifier.LoadSirInit
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

    @MavericksInit
    @Inject
    lateinit var mavericksInit: IAppInit

    @LoadSirInit
    @Inject
    lateinit var loadSirInit: IAppInit

    @FlutterInit
    @Inject
    lateinit var flutterInit: IAppInit


    override fun onCreate() {
        super.onCreate()

        baseInit.init(this)

        appInit.init(this)

        eventInit.init(this)
        mavericksInit.init(this)
        loadSirInit.init(this)

        flutterInit.init(this) // FlutterEngine
    }
}