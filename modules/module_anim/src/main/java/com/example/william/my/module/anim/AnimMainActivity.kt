package com.example.william.my.module.anim

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Anim.Main)
class AnimMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("ValueAnimator", RouterPath.Anim.ValueAnimator))
        routerItems.add(RouterItem("ObjectAnimator", RouterPath.Anim.ObjectAnimator))
        routerItems.add(RouterItem("AnimatorSet", RouterPath.Anim.AnimatorSet))
        routerItems.add(RouterItem("Transition", RouterPath.Anim.Transition))
        routerItems.add(RouterItem("RenderEffect", RouterPath.Anim.RenderEffect))
        routerItems.add(RouterItem("RenderScript", RouterPath.Anim.RenderScript))
        return routerItems
    }
}