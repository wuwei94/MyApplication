package com.example.william.my.module.event

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 事件总线演示入口
 *
 * 聚合了主流事件总线方案（EventBus、RxJava、LiveData、Flow）的演示示例。
 */
@Route(path = RouterPath.Event.Main)
class EventMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = ArrayList<RouterItem>()
        routerItems.add(RouterItem("EventBus", RouterPath.Event.EventBus))
        routerItems.add(RouterItem("RxEventBus", RouterPath.Event.RxEventBus))
        routerItems.add(RouterItem("LiveEventBus", RouterPath.Event.LiveEventBus))
        routerItems.add(RouterItem("FlowEventBus", RouterPath.Event.FlowEventBus))
        return routerItems
    }
}