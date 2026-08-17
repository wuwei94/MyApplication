package com.example.william.my.module.tab

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Tab.Main)
class TabMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("TabHost", RouterPath.Tab.TabHost))
        routerItems.add(RouterItem("FrameLayoutTab", RouterPath.Tab.FrameLayoutTab))
        routerItems.add(RouterItem("ViewPagerTab", RouterPath.Tab.ViewPagerTab))
        routerItems.add(RouterItem("ViewPager2Tab", RouterPath.Tab.ViewPager2Tab))
        routerItems.add(RouterItem("BottomNav", RouterPath.Tab.BottomNav))
        return routerItems
    }
}
