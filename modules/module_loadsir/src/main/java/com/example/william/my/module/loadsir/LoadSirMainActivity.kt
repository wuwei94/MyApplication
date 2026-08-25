package com.example.william.my.module.loadsir

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.LoadSir.Main)
class LoadSirMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 多状态页面 ──", ""))
        routerItems.add(RouterItem("LoadSir", RouterPath.LoadSir.LoadSir))
        routerItems.add(RouterItem("LoadSirFragment", RouterPath.LoadSir.LoadSirFragment))
        return routerItems
    }
}
