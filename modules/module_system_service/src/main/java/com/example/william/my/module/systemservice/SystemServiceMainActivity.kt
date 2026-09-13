package com.example.william.my.module.systemservice

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 系统服务模块入口页
 *
 * 展示通知、权限、悬浮窗等系统服务的示例列表。
 */
@Route(path = RouterPath.SystemService.Main)
class SystemServiceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // 通知
        routerItems.add(RouterItem("── 通知 ──", ""))
        routerItems.add(RouterItem("Notification（通知渠道与前台通知）", RouterPath.SystemService.Notification))

        // 权限
        routerItems.add(RouterItem("── 权限 ──", ""))
        routerItems.add(RouterItem("Permission（运行时权限 Jetpack 契约）", RouterPath.SystemService.Permission))
        routerItems.add(RouterItem("PermissionX（运行时权限 链式开源库）", RouterPath.SystemService.PermissionX))

        // 悬浮窗
        routerItems.add(RouterItem("── 悬浮窗 ──", ""))
        routerItems.add(RouterItem("FloatWindow（系统级悬浮窗 原生实现）", RouterPath.SystemService.FloatWindow))
        routerItems.add(RouterItem("EasyFloat（应用内悬浮窗 第三方开源库）", RouterPath.SystemService.EasyFloat))

        return routerItems
    }
}
