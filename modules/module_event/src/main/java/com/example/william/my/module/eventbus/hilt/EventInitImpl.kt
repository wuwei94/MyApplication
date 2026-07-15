package com.example.william.my.module.eventbus.hilt

import android.app.Application
import com.example.william.my.core.eventbus.flow.FlowEventBus
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import javax.inject.Inject

class EventInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        initFlowEventBus(app)
    }

    override fun getApp(): Application {
        return mApp
    }

    private fun initFlowEventBus(app: Application) {
        FlowEventBus.init(app)
    }
}