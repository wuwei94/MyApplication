package com.example.william.my.module.http

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Http.Main)
class HttpMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("HttpURL", RouterPath.Http.HttpURL)
        )
        routerItems.add(
            RouterItem("Volley", RouterPath.Http.Volley)
        )
        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("Ktor", RouterPath.Http.Ktor)
        )
        routerItems.add(
            RouterItem("KtorClient", RouterPath.Http.KtorClient)
        )
        return routerItems
    }
}
