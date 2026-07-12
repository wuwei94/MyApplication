package com.example.william.my.module.sync

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Sync.Main)
class SyncActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AsyncTask", RouterPath.Sync.AsyncTask))
        routerItems.add(RouterItem("HandlerThread", RouterPath.Sync.HandlerThread))
        routerItems.add(RouterItem("JobScheduler", RouterPath.Sync.JobScheduler))
        return routerItems
    }
}
