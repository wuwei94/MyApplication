package com.example.william.my.module.opensource

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.OpenSource.Main)
class OpenSourceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 工具 ──", ""))
        routerItems.add(RouterItem("RxJava", RouterPath.OpenSource.RxJava))

        return routerItems
    }
}
