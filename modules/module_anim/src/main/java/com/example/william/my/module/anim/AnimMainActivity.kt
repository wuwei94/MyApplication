package com.example.william.my.module.anim

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 动画模块入口页
 *
 * 展示各类动画技术的示例列表，点击列表项跳转到对应的示例页。
 */
@Route(path = RouterPath.Anim.Main)
class AnimMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 系统原生 ──", ""))
        routerItems.add(RouterItem("ValueAnimator（属性值动画）", RouterPath.Anim.ValueAnimator))
        routerItems.add(RouterItem("ObjectAnimator（对象属性动画）", RouterPath.Anim.ObjectAnimator))
        routerItems.add(RouterItem("AnimatorSet（组合动画集合）", RouterPath.Anim.AnimatorSet))
        routerItems.add(RouterItem("Transition（转场过渡动画）", RouterPath.Anim.Transition))
        routerItems.add(RouterItem("RenderEffect（Android 12+ 渲染特效）", RouterPath.Anim.RenderEffect))
        routerItems.add(RouterItem("RenderScript（底层模糊与图形计算）", RouterPath.Anim.RenderScript))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 第三方动画库 ──", ""))
        routerItems.add(RouterItem("PAG（Tencent 动效文件渲染）", RouterPath.Anim.Pag))
        routerItems.add(RouterItem("Lottie（Airbnb 矢量 JSON 动画）", RouterPath.Anim.Lottie))
        routerItems.add(RouterItem("SVGAPlayer（SVGA 高性能动画）", RouterPath.Anim.SvgaPlayer))
        return routerItems
    }
}
