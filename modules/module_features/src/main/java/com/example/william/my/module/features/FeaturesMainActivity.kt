package com.example.william.my.module.features

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Features.Main)
class FeaturesMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Turntable", RouterPath.Features.Turntable))
        routerItems.add(RouterItem("MicAnimation", RouterPath.Features.MicAnimation))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Camera", RouterPath.Features.Camera))
        routerItems.add(RouterItem("Crop", RouterPath.Features.Crop))
        return routerItems
    }
}
