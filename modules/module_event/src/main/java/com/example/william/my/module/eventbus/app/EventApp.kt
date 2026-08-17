package com.example.william.my.module.eventbus.app

import com.example.william.my.core.eventbus.flow.FlowEventBus
import com.example.william.my.core.base.app.BaseAppInit

/**
 * Event 模块 Application 初始化
 */
class EventApp : BaseAppInit() {

    override fun init() {
        super.init()

        FlowEventBus.init(app)
    }
}