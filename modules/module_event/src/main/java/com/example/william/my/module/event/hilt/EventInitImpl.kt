package com.example.william.my.module.event.hilt

import android.app.Application
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.eventbus.flow.FlowEventBus
import javax.inject.Inject

/**
 * Event 模块 Hilt 初始化实现
 */
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