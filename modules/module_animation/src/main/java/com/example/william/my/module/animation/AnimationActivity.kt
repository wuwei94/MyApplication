package com.example.william.my.module.animation

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Animation.Main)
class AnimationActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Animator", RouterPath.Animation.Animator))
        routerItems.add(RouterItem("Transition", RouterPath.Animation.Transition))
        routerItems.add(RouterItem("RenderEffect", RouterPath.Animation.RenderEffect))
        routerItems.add(RouterItem("RenderScript", RouterPath.Animation.RenderScript))
        return routerItems
    }
}