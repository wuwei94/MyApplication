package com.example.william.my.module.systemservice

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.SystemService.Main)
class SystemServiceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Notification", RouterPath.SystemService.Notification))
        routerItems.add(RouterItem("Permission", RouterPath.SystemService.Permission))
        routerItems.add(RouterItem("SecureKey (Android Keystore)", RouterPath.SystemService.SecureKey))
        return routerItems
    }
}
