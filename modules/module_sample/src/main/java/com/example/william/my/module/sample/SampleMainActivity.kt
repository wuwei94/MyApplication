package com.example.william.my.module.sample

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Sample.Main)
class SampleMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // 1. 业务技巧与系统特性
        routerItems.add(RouterItem("Hook", RouterPath.Sample.Hook))
        routerItems.add(RouterItem("Typeface", RouterPath.Sample.Typeface))
        routerItems.add(RouterItem("FloatWindow", RouterPath.Sample.FloatWindow))

        routerItems.add(RouterItem("", ""))

        // 2. 性能优化与异步调度
        routerItems.add(RouterItem("LruCache", RouterPath.Sample.LruCache))
        routerItems.add(RouterItem("IdleHandler", RouterPath.Sample.IdleHandler))
        routerItems.add(RouterItem("AsyncLayoutInflater", RouterPath.Sample.AsyncLayoutInflater))

        routerItems.add(RouterItem("", ""))

        // 3. RecyclerView 架构与复用优化
        routerItems.add(RouterItem("ConcatAdapter", RouterPath.Sample.ConcatAdapter))
        routerItems.add(RouterItem("RecycledViewPool", RouterPath.Sample.RecycledViewPool))
        routerItems.add(RouterItem("DiffUtil", RouterPath.Sample.DiffUtil))

        return routerItems
    }
}
