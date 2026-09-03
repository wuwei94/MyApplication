package com.example.william.my.application.app

import android.app.Application
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.AppInit
import com.example.william.my.core.base.app.hilt.qualifier.BaseInit
import com.example.william.my.core.base.app.hilt.qualifier.EventInit
import com.example.william.my.core.base.app.hilt.qualifier.FlutterInit
import com.example.william.my.core.base.app.hilt.qualifier.LoadSirInit
import com.example.william.my.core.base.app.hilt.qualifier.MavericksInit
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
