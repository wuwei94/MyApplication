package com.example.william.my.module.service

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.ServiceDemo.Main)
class ServiceDemoActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Service", RouterPath.ServiceDemo.Service))
        routerItems.add(RouterItem("Messenger", RouterPath.ServiceDemo.Messenger))
        return routerItems
    }
}
