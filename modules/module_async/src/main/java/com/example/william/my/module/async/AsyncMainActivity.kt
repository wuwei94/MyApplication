package com.example.william.my.module.async

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Async.Main)
class AsyncMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AsyncTask（经典异步任务）", RouterPath.Async.AsyncTask))
        routerItems.add(RouterItem("HandlerThread（Handler + Thread 循环机制）", RouterPath.Async.HandlerThread))
        return routerItems
    }
}
