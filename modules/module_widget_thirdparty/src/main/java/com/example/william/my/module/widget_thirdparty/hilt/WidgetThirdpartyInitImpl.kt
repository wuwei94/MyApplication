package com.example.william.my.module.widget_thirdparty.hilt

import android.app.Application
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import javax.inject.Inject

class WidgetThirdpartyInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app
    }

    override fun getApp(): Application {
        return mApp
    }
}
