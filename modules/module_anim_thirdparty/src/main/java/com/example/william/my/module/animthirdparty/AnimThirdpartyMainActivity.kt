package com.example.william.my.module.animthirdparty

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 第三方动画库模块入口 — 导航到 PAG、Lottie、SVGA 三个动画库示例页面。
 *
 * 本模块聚焦「第三方动画库」主题，与 module_anim（系统原生动画）形成对照。
 */
@Route(path = RouterPath.AnimThirdparty.Main)
class AnimThirdpartyMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("Pag", RouterPath.AnimThirdparty.Pag),
            RouterItem("Lottie", RouterPath.AnimThirdparty.Lottie),
            RouterItem("SVGAPlayer", RouterPath.AnimThirdparty.SVGAPlayer)
        )
    }
}
