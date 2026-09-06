package com.example.william.my.module.component

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 组件模块入口页
 *
 * 展示 ActivityResult、广播、Service 等 Android 组件的示例列表。
 */
@Route(path = RouterPath.Component.Main)
class ComponentMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 原生四大组件 ──", ""))
        routerItems.add(RouterItem("Broadcast（广播接收与动态注册）", RouterPath.Component.Broadcast))
        routerItems.add(RouterItem("Service（绑定服务与前台服务）", RouterPath.Component.Service))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Jetpack 交互契约 ──", ""))
        routerItems.add(RouterItem("ActivityResult（新版结果回调契约）", RouterPath.Component.ActivityResult))
        routerItems.add(RouterItem("OnBackPressed（返回键分发与拦截）", RouterPath.Component.OnBackPressed))
        return routerItems
    }
}
