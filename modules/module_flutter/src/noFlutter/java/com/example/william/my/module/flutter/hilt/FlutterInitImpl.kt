package com.example.william.my.module.flutter.hilt

import android.app.Application
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import javax.inject.Inject

class FlutterInitImpl @Inject constructor() : IAppInit {

    private lateinit var app: Application

    override fun init(app: Application) {
        this.app = app
    }

    override fun getApp(): Application {
        return app
    }
}
