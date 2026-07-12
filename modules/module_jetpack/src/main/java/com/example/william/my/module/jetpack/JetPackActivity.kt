package com.example.william.my.module.jetpack

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.JetPack.Main)
class JetPackActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("DataStore", RouterPath.JetPack.DataStore))
        routerItems.add(RouterItem("WorkManager", RouterPath.JetPack.WorkManager))
        routerItems.add(RouterItem("Room", RouterPath.JetPack.Room))
        routerItems.add(RouterItem("Paging", RouterPath.JetPack.Paging))
        routerItems.add(RouterItem("Hilt", RouterPath.JetPack.Hilt))
        return routerItems
    }
}
