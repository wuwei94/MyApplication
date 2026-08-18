package com.example.william.my.module.feature

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Feature.Main)
class FeatureMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Turntable", RouterPath.Feature.Turntable))
        routerItems.add(RouterItem("MicAnimation", RouterPath.Feature.MicAnimation))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Camera", RouterPath.Feature.Camera))
        routerItems.add(RouterItem("Crop", RouterPath.Feature.Crop))
        return routerItems
    }
}
