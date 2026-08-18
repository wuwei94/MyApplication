package com.example.william.my.module.network

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Network.Main)
class NetworkMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("HttpURL", RouterPath.Network.HttpURL)
        )
        routerItems.add(
            RouterItem("Volley", RouterPath.Network.Volley)
        )
        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("Ktor", RouterPath.Network.Ktor)
        )
        routerItems.add(
            RouterItem("KtorClient", RouterPath.Network.KtorClient)
        )
        return routerItems
    }
}
