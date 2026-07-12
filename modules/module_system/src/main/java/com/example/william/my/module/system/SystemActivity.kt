package com.example.william.my.module.system

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.System.Main)
class SystemActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Notification", RouterPath.System.Notification))
        routerItems.add(RouterItem("Permission", RouterPath.System.Permission))
        routerItems.add(RouterItem("SecureKey", RouterPath.System.SecureKey))
        return routerItems
    }
}
