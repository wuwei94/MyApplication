package com.example.william.my.module.systemservice

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.SystemService.Main)
class SystemServiceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Notification（通知渠道与前台通知）", RouterPath.SystemService.Notification))
        routerItems.add(RouterItem("Permission（运行时权限 Jetpack 契约）", RouterPath.SystemService.Permission))
        routerItems.add(RouterItem("PermissionX（运行时权限 链式开源库）", RouterPath.SystemService.PermissionX))
        routerItems.add(RouterItem("SecureKey（Android Keystore 硬件级安全密钥）", RouterPath.SystemService.SecureKey))
        return routerItems
    }
}
