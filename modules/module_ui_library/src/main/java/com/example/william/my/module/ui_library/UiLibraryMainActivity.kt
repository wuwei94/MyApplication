package com.example.william.my.module.ui_library

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * UI 库模块入口 — 导航到各第三方 UI 控件库示例页面。
 */
@Route(path = RouterPath.UiLibrary.Main)
class UiLibraryMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Banner", RouterPath.UiLibrary.Banner))
        routerItems.add(RouterItem("RealtimeBlurView", RouterPath.UiLibrary.RealtimeBlurView))
        routerItems.add(RouterItem("CountdownView", RouterPath.UiLibrary.CountdownView))
        routerItems.add(RouterItem("PopWindow", RouterPath.UiLibrary.PopWindow))
        routerItems.add(RouterItem("ShadowLayout", RouterPath.UiLibrary.ShadowLayout))
        routerItems.add(RouterItem("SwipeLayout", RouterPath.UiLibrary.SwipeLayout))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("LoadSir", RouterPath.UiLibrary.LoadSir))
        return routerItems
    }
}
