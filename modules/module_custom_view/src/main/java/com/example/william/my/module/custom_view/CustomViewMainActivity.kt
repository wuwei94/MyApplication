package com.example.william.my.module.custom_view

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.CustomView.Main)
class CustomViewMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AlertDialog", RouterPath.CustomView.AlertDialog))
        routerItems.add(RouterItem("BlurView", RouterPath.CustomView.BlurView))
        routerItems.add(RouterItem("InfiniteImage", RouterPath.CustomView.InfiniteImage))
        routerItems.add(RouterItem("MarqueeView", RouterPath.CustomView.MarqueeView))
        routerItems.add(RouterItem("Sensor3DView", RouterPath.CustomView.Sensor3DView))
        routerItems.add(RouterItem("Spinner", RouterPath.CustomView.Spinner))
        routerItems.add(RouterItem("TitleBar", RouterPath.CustomView.TitleBar))
        routerItems.add(RouterItem("VerifyCode", RouterPath.CustomView.VerifyCode))
        routerItems.add(RouterItem("NinePatch", RouterPath.CustomView.NinePatch))
        return routerItems
    }
}
