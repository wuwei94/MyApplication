package com.example.william.my.module.widget

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 控件模块入口页
 *
 * 展示 Dialog、RecyclerView、ViewPager、WebView 等控件的示例列表。
 */
@Route(path = RouterPath.Widget.Main)
class WidgetMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Appbar（AppBarLayout 联动折叠）", RouterPath.Widget.Appbar))
        routerItems.add(RouterItem("Dialog（标准对话框）", RouterPath.Widget.Dialog))
        routerItems.add(RouterItem("PopWindow（PopupWindow 浮层）", RouterPath.Widget.PopWindow))
        routerItems.add(RouterItem("FlexBox（流式弹性盒布局）", RouterPath.Widget.FlexBox))
        routerItems.add(RouterItem("RecyclerView（标准列表）", RouterPath.Widget.RecyclerView))
        routerItems.add(RouterItem("RecyclerViewNested（嵌套滑动列表）", RouterPath.Widget.RecyclerViewNested))
        routerItems.add(RouterItem("ViewPager（传统分页滑动）", RouterPath.Widget.ViewPager))
        routerItems.add(RouterItem("ViewPager2（新版基于 RV 分页滑动）", RouterPath.Widget.ViewPager2))
        routerItems.add(RouterItem("ViewFlipper（视图翻转轮播）", RouterPath.Widget.ViewFlipper))
        routerItems.add(RouterItem("WebView（网页加载与 JS 交互）", RouterPath.Widget.WebView))
        routerItems.add(RouterItem("FloatWindow（系统悬浮窗权限与实现）", RouterPath.Widget.FloatWindow))
        return routerItems
    }
}
