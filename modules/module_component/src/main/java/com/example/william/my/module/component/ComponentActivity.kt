package com.example.william.my.module.component

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Component.Main)
class ComponentActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Broadcast", RouterPath.Component.Broadcast))
        routerItems.add(RouterItem("ActivityResult", RouterPath.Component.ActivityResult))
        routerItems.add(RouterItem("OnBackPressed", RouterPath.Component.OnBackPressed))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Service", RouterPath.Component.Service))
        routerItems.add(RouterItem("Messenger", RouterPath.Component.Messenger))
        return routerItems
    }
}
