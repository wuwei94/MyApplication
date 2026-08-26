package com.example.william.my.module.component

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Component.Main)
class ComponentMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 原生四大组件 ──", ""))
        routerItems.add(RouterItem("Broadcast", RouterPath.Component.Broadcast))
        routerItems.add(RouterItem("Service", RouterPath.Component.Service))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Jetpack 交互契约 ──", ""))
        routerItems.add(RouterItem("ActivityResult", RouterPath.Component.ActivityResult))
        routerItems.add(RouterItem("OnBackPressed", RouterPath.Component.OnBackPressed))
        return routerItems
    }
}
