package com.example.william.my.module.widget

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Widget.Main)
class WidgetMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Appbar", RouterPath.Widget.Appbar))
        routerItems.add(RouterItem("Dialog", RouterPath.Widget.Dialog))
        routerItems.add(RouterItem("PopWindow", RouterPath.Widget.PopWindow))
        routerItems.add(RouterItem("FlexBox", RouterPath.Widget.FlexBox))
        routerItems.add(RouterItem("RecyclerView", RouterPath.Widget.RecyclerView))
        routerItems.add(RouterItem("RecyclerViewNested", RouterPath.Widget.RecyclerViewNested))
        routerItems.add(RouterItem("ViewPager", RouterPath.Widget.ViewPager))
        routerItems.add(RouterItem("ViewPager2", RouterPath.Widget.ViewPager2))
        routerItems.add(RouterItem("ViewFlipper", RouterPath.Widget.ViewFlipper))
        routerItems.add(RouterItem("WebView", RouterPath.Widget.WebView))
        return routerItems
    }
}
