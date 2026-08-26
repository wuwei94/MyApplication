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

        // 1. 基础与动效控件
        routerItems.add(RouterItem("── 基础与动效控件 ──", ""))
        routerItems.add(RouterItem("Banner（轮播图）", RouterPath.WidgetThirdparty.Banner))
        routerItems.add(RouterItem("CountdownView（倒计时）", RouterPath.WidgetThirdparty.CountdownView))
        routerItems.add(RouterItem("EasyFloat（全局/应用内悬浮窗）", RouterPath.WidgetThirdparty.EasyFloat))
        routerItems.add(RouterItem("PhotoView（手势缩放图片）", RouterPath.WidgetThirdparty.PhotoView))
        routerItems.add(RouterItem("RealtimeBlurView（实时动态高斯模糊）", RouterPath.WidgetThirdparty.RealtimeBlurView))
        routerItems.add(RouterItem("ShadowLayout（万能阴影与圆角布局）", RouterPath.WidgetThirdparty.ShadowLayout))
        routerItems.add(RouterItem("SwipeLayout（侧滑操作菜单）", RouterPath.WidgetThirdparty.SwipeLayout))

        routerItems.add(RouterItem("", ""))

        // 2. 选择器与拾取器
        routerItems.add(RouterItem("── 选择器 ──", ""))
        routerItems.add(RouterItem("CityPicker（三级城市选择器）", RouterPath.WidgetThirdparty.CityPicker))
        routerItems.add(RouterItem("PickerView（时间与条件滚轮选择器）", RouterPath.WidgetThirdparty.PickerView))
        routerItems.add(RouterItem("PictureSelector（图片/多媒体选择器）", RouterPath.WidgetThirdparty.PictureSelector))

        routerItems.add(RouterItem("", ""))

        // 3. 页面状态管理
        routerItems.add(RouterItem("── 页面状态管理 ──", ""))
        routerItems.add(RouterItem("LoadSir（多状态页面管理）", RouterPath.WidgetThirdparty.LoadSir))
        routerItems.add(RouterItem("LoadSir Fragment（Fragment 状态管理）", RouterPath.WidgetThirdparty.LoadSirFragment))

        return routerItems
    }
}
