package com.example.william.my.module.performance

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Performance.Main)
class PerformanceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // 1. 性能优化与异步调度
        routerItems.add(RouterItem("LruCache", RouterPath.Performance.LruCache))
        routerItems.add(RouterItem("IdleHandler", RouterPath.Performance.IdleHandler))
        routerItems.add(RouterItem("AsyncLayoutInflater", RouterPath.Performance.AsyncLayoutInflater))

        routerItems.add(RouterItem("", ""))

        // 2. RecyclerView 架构与复用优化
        routerItems.add(RouterItem("ConcatAdapter", RouterPath.Performance.ConcatAdapter))
        routerItems.add(RouterItem("RecycledViewPool", RouterPath.Performance.RecycledViewPool))
        routerItems.add(RouterItem("DiffUtil", RouterPath.Performance.DiffUtil))

        return routerItems
    }
}
