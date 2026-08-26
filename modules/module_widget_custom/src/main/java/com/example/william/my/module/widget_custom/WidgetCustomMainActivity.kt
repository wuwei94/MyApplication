package com.example.william.my.module.widget_custom

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.WidgetCustom.Main)
class WidgetCustomMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AlertDialog（自定义圆角对话框）", RouterPath.WidgetCustom.AlertDialog))
        routerItems.add(RouterItem("CustomPopWindow（自定义弹窗）", RouterPath.WidgetCustom.CustomPopWindow))
        routerItems.add(RouterItem("BlurView（局部背景高斯模糊）", RouterPath.WidgetCustom.BlurView))
        routerItems.add(RouterItem("InfiniteImage（无限循环滚动背景）", RouterPath.WidgetCustom.InfiniteImage))
        routerItems.add(RouterItem("MarqueeView（跑马灯垂直滚动条）", RouterPath.WidgetCustom.MarqueeView))
        routerItems.add(RouterItem("Sensor3DView（重力感应 3D 视差图）", RouterPath.WidgetCustom.Sensor3DView))
        routerItems.add(RouterItem("Spinner（自定义下拉列表）", RouterPath.WidgetCustom.Spinner))
        routerItems.add(RouterItem("TitleBar（通用导航栏封装）", RouterPath.WidgetCustom.TitleBar))
        routerItems.add(RouterItem("VerifyCode（图形验证码绘制）", RouterPath.WidgetCustom.VerifyCode))
        routerItems.add(RouterItem("NinePatch（.9 气泡与图层绘制）", RouterPath.WidgetCustom.NinePatch))
        return routerItems
    }
}
