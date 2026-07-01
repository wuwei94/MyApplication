package com.example.william.my.module.eventbus

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Event.Main)
class EventActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = ArrayList<RouterItem>()
        routerItems.add(RouterItem("EventBus", RouterPath.Event.EventBus))
        routerItems.add(RouterItem("RxEventBus", RouterPath.Event.RxEventBus))
        routerItems.add(RouterItem("LiveEventBus", RouterPath.Event.LiveEventBus))
        routerItems.add(RouterItem("FlowEventBus", RouterPath.Event.FlowEventBus))
        return routerItems
    }
}