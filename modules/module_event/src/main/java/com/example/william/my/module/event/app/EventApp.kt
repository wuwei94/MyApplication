package com.example.william.my.module.event.app

import com.example.william.my.core.base.app.BaseAppInit
import com.example.william.my.core.eventbus.flow.FlowEventBus

/**
 * Event 模块 Application 初始化
 */
class EventApp : BaseAppInit() {

    override fun init() {
        super.init()

        FlowEventBus.init(app)
    }
}
