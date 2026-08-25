package com.example.william.my.module.httpbasic

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.HttpBasic.Main)
class HttpBasicMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("HttpURL", RouterPath.HttpBasic.HttpURL)
        )
        routerItems.add(
            RouterItem("Volley", RouterPath.HttpBasic.Volley)
        )
        return routerItems
    }
}
