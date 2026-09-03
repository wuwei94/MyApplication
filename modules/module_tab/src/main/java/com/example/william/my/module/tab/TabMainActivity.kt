package com.example.william.my.module.tab

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Tab.Main)
class TabMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // 1. 系统原生与常规联动
        routerItems.add(RouterItem("── 原生与标准联动 ──", ""))
        routerItems.add(RouterItem("TabHost（经典选项卡）", RouterPath.Tab.TabHost))
        routerItems.add(RouterItem("FrameLayout + RadioGroup", RouterPath.Tab.FrameLayoutTab))
        routerItems.add(RouterItem("ViewPager + RadioGroup", RouterPath.Tab.ViewPagerTab))
        routerItems.add(RouterItem("ViewPager2 + RadioGroup", RouterPath.Tab.ViewPager2Tab))
        routerItems.add(RouterItem("BottomNavigation + Fragment", RouterPath.Tab.BottomNavigation))

        routerItems.add(RouterItem("", ""))

        // 2. 第三方 Tab 库
        routerItems.add(RouterItem("── 第三方库 ──", ""))
        routerItems.add(RouterItem("FlycoTabLayout", RouterPath.Tab.FlycoTabLayout))

        return routerItems
    }
}
