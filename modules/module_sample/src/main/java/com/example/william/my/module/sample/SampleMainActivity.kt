package com.example.william.my.module.sample

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Sample.Main)
class SampleMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Hook", RouterPath.Sample.Hook))
        routerItems.add(RouterItem("Typeface", RouterPath.Sample.Typeface))
        routerItems.add(RouterItem("FloatWindow", RouterPath.Sample.FloatWindow))
        return routerItems
    }
}
