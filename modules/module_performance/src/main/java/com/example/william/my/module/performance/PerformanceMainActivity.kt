package com.example.william.my.module.performance

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Performance.Main)
class PerformanceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // 1. 启动与初始化优化
        routerItems.add(RouterItem("── 启动与初始化优化 ──", ""))
        routerItems.add(RouterItem("ContentProvider 自动初始化", RouterPath.Performance.ContentProvider))
        routerItems.add(RouterItem("App Startup", RouterPath.Performance.Startup))
        routerItems.add(RouterItem("Baseline Profiles", RouterPath.Performance.BaselineProfiles))
        routerItems.add(RouterItem("IdleHandler 空闲调度", RouterPath.Performance.IdleHandler))

        routerItems.add(RouterItem("", ""))

        // 2. 布局与渲染优化
        routerItems.add(RouterItem("── 布局与渲染优化 ──", ""))
        routerItems.add(RouterItem("AsyncLayoutInflater", RouterPath.Performance.AsyncLayoutInflater))

        routerItems.add(RouterItem("", ""))

        // 3. 内存与列表优化
        routerItems.add(RouterItem("── 内存与列表优化 ──", ""))
        routerItems.add(RouterItem("LruCache 内存缓存", RouterPath.Performance.LruCache))
        routerItems.add(RouterItem("ConcatAdapter 模块化列表", RouterPath.Performance.ConcatAdapter))
        routerItems.add(RouterItem("RecycledViewPool 共享视图池", RouterPath.Performance.RecycledViewPool))
        routerItems.add(RouterItem("DiffUtil 差量刷新", RouterPath.Performance.DiffUtil))

        return routerItems
    }
}
