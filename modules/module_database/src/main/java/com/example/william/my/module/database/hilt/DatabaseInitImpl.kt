package com.example.william.my.module.database.hilt

import android.app.Application
import com.example.william.my.lib.hilt.interfaces.IAppInit
import com.example.william.my.module.database.objectbox.ObjectBox
import javax.inject.Inject

class DatabaseInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        ObjectBox.init(app)
    }

    override fun getApp(): Application {
        return mApp
    }
}