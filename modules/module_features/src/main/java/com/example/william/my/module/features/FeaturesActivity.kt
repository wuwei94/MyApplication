package com.example.william.my.module.features

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Features.Main)
class FeaturesActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Turntable", RouterPath.Features.Business.Turntable))
        routerItems.add(RouterItem("MicAnimation", RouterPath.Features.Business.MicAnimation))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Camera", RouterPath.Features.Media.Camera))
        routerItems.add(RouterItem("Crop", RouterPath.Features.Media.Crop))
        return routerItems
    }
}
