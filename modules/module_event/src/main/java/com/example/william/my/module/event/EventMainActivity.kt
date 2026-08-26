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
        routerItems.add(RouterItem("EventBus（GreenRobot 经典发布/订阅总线）", RouterPath.Event.EventBus))
        routerItems.add(RouterItem("RxEventBus（基于 RxJava 响应式流实现）", RouterPath.Event.RxEventBus))
        routerItems.add(RouterItem("LiveEventBus（基于 LiveData 生命周期感知实现）", RouterPath.Event.LiveEventBus))
        routerItems.add(RouterItem("FlowEventBus（基于 Kotlin Flow 现代事件总线）", RouterPath.Event.FlowEventBus))
        return routerItems
    }
}