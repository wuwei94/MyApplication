package com.example.william.my.module.widget_custom

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.WidgetCustom.Main)
class WidgetCustomMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AlertDialog", RouterPath.WidgetCustom.AlertDialog))
        routerItems.add(RouterItem("BlurView", RouterPath.WidgetCustom.BlurView))
        routerItems.add(RouterItem("InfiniteImage", RouterPath.WidgetCustom.InfiniteImage))
        routerItems.add(RouterItem("MarqueeView", RouterPath.WidgetCustom.MarqueeView))
        routerItems.add(RouterItem("Sensor3DView", RouterPath.WidgetCustom.Sensor3DView))
        routerItems.add(RouterItem("Spinner", RouterPath.WidgetCustom.Spinner))
        routerItems.add(RouterItem("TitleBar", RouterPath.WidgetCustom.TitleBar))
        routerItems.add(RouterItem("VerifyCode", RouterPath.WidgetCustom.VerifyCode))
        routerItems.add(RouterItem("NinePatch", RouterPath.WidgetCustom.NinePatch))
        return routerItems
    }
}
