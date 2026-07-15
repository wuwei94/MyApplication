package com.example.william.my.module.eventbus.app

import com.example.william.my.core.eventbus.flow.FlowEventBus
import com.example.william.my.core.base.app.BaseAppInit

class EventApp : BaseAppInit() {

    override fun init() {
        super.init()

        FlowEventBus.init(app)
    }
}