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
        routerItems.add(RouterItem("ContentProvider 自动初始化（时序机制与多 Provider 耗时分析）", RouterPath.Performance.ContentProvider))
        routerItems.add(RouterItem("App Startup（InitializationProvider 聚合与 DAG 拓扑排序）", RouterPath.Performance.Startup))
        routerItems.add(RouterItem("Baseline Profiles（ART 预编译 AOT 加速与 ProfileInstaller）", RouterPath.Performance.BaselineProfiles))
        routerItems.add(RouterItem("IdleHandler 空闲调度（主线程延迟初始化）", RouterPath.Performance.IdleHandler))

        routerItems.add(RouterItem("", ""))

        // 2. 布局与渲染优化
        routerItems.add(RouterItem("── 布局与渲染优化 ──", ""))
        routerItems.add(RouterItem("AsyncLayoutInflater（异步布局解析与视图池预加载）", RouterPath.Performance.AsyncLayoutInflater))

        routerItems.add(RouterItem("", ""))

        // 3. 内存与列表优化
        routerItems.add(RouterItem("── 内存与列表优化 ──", ""))
        routerItems.add(RouterItem("LruCache 内存缓存（Cache-Aside 与容量淘汰）", RouterPath.Performance.LruCache))
        routerItems.add(RouterItem("ConcatAdapter（模块化列表组合与 ViewType 隔离）", RouterPath.Performance.ConcatAdapter))
        routerItems.add(RouterItem("RecycledViewPool（跨列表/Tab 共享视图池）", RouterPath.Performance.RecycledViewPool))
        routerItems.add(RouterItem("DiffUtil（列表差量计算与局部刷新）", RouterPath.Performance.DiffUtil))

        return routerItems
    }
}
