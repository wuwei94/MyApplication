package com.example.william.my.module.ktor

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Ktor.Main)
class KtorMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Ktor", RouterPath.Ktor.Ktor))
        routerItems.add(RouterItem("KtorClient", RouterPath.Ktor.KtorClient))
        return routerItems
    }
}
