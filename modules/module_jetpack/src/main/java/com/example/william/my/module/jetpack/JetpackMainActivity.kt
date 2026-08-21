package com.example.william.my.module.jetpack

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Jetpack.Main)
class JetpackMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("DataStore", RouterPath.Jetpack.DataStore))
        routerItems.add(RouterItem("Paging", RouterPath.Jetpack.Paging))
        return routerItems
    }
}
