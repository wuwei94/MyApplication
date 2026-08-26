package com.example.william.my.module.widget_thirdparty

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * UI 库模块入口 — 导航到各第三方 UI 控件库示例页面。
 */
@Route(path = RouterPath.WidgetThirdparty.Main)
class WidgetThirdpartyMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Banner", RouterPath.WidgetThirdparty.Banner))
        routerItems.add(RouterItem("CountdownView", RouterPath.WidgetThirdparty.CountdownView))
        routerItems.add(RouterItem("EasyFloat", RouterPath.WidgetThirdparty.EasyFloat))
        routerItems.add(RouterItem("PhotoView", RouterPath.WidgetThirdparty.PhotoView))
        routerItems.add(RouterItem("RealtimeBlurView", RouterPath.WidgetThirdparty.RealtimeBlurView))
        routerItems.add(RouterItem("ShadowLayout", RouterPath.WidgetThirdparty.ShadowLayout))
        routerItems.add(RouterItem("SwipeLayout", RouterPath.WidgetThirdparty.SwipeLayout))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("CityPicker", RouterPath.WidgetThirdparty.CityPicker))
        routerItems.add(RouterItem("PickerView", RouterPath.WidgetThirdparty.PickerView))
        routerItems.add(RouterItem("PictureSelector", RouterPath.WidgetThirdparty.PictureSelector))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("LoadSir", RouterPath.WidgetThirdparty.LoadSir))
        routerItems.add(RouterItem("LoadSirFragment", RouterPath.WidgetThirdparty.LoadSirFragment))

        return routerItems
    }
}
