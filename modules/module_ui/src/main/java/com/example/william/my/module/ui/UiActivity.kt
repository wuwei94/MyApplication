package com.example.william.my.module.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.UI.Main)
class UiActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Appbar", RouterPath.UI.Appbar))
        routerItems.add(RouterItem("Dialog", RouterPath.UI.Dialog))
        routerItems.add(RouterItem("FlexBox", RouterPath.UI.FlexBox))
        routerItems.add(RouterItem("RecyclerView", RouterPath.UI.RecyclerView))
        routerItems.add(RouterItem("ViewFlipper", RouterPath.UI.ViewFlipper))
        routerItems.add(RouterItem("ViewPager", RouterPath.UI.ViewPager))
        routerItems.add(RouterItem("ViewPager2", RouterPath.UI.ViewPager2))
        routerItems.add(RouterItem("WebView", RouterPath.UI.WebView))
        routerItems.add(RouterItem("RecyclerViewNested", RouterPath.UI.RecyclerViewNested))
        return routerItems
    }
}
