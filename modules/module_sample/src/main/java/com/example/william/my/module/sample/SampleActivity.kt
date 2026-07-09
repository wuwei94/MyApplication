package com.example.william.my.module.sample

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Sample.Main)
class SampleActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Appbar", RouterPath.Sample.UI.Appbar))
        routerItems.add(RouterItem("Dialog", RouterPath.Sample.UI.Dialog))
        routerItems.add(RouterItem("FlexBox", RouterPath.Sample.UI.FlexBox))
        routerItems.add(RouterItem("Fragment1", RouterPath.Sample.UI.Fragment1))
        routerItems.add(RouterItem("Fragment2", RouterPath.Sample.UI.Fragment2))
        routerItems.add(RouterItem("FragmentTabHost", RouterPath.Sample.UI.FragmentTabHost))
        routerItems.add(RouterItem("RecyclerView", RouterPath.Sample.UI.RecyclerView))
        routerItems.add(RouterItem("ViewFlipper", RouterPath.Sample.UI.ViewFlipper))
        routerItems.add(RouterItem("ViewPager", RouterPath.Sample.UI.ViewPager))
        routerItems.add(RouterItem("ViewPager2", RouterPath.Sample.UI.ViewPager2))
        routerItems.add(RouterItem("WebView", RouterPath.Sample.UI.WebView))
        routerItems.add(RouterItem("FragmentViewPager", RouterPath.Sample.UI.FragmentViewPager))
        routerItems.add(RouterItem("RecyclerViewNested", RouterPath.Sample.UI.RecyclerViewNested))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("AsyncTask", RouterPath.Sample.Background.AsyncTask))
        routerItems.add(RouterItem("HandlerThread", RouterPath.Sample.Background.HandlerThread))
        routerItems.add(RouterItem("JobScheduler", RouterPath.Sample.Background.JobScheduler))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Broadcast", RouterPath.Sample.Communication.Broadcast))
        routerItems.add(RouterItem("Messenger", RouterPath.Sample.Communication.Messenger))
        routerItems.add(RouterItem("Service", RouterPath.Sample.Communication.Service))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("ActivityResult", RouterPath.Sample.System.ActivityResult))
        routerItems.add(RouterItem("OnBackPressed", RouterPath.Sample.System.OnBackPressed))
        routerItems.add(RouterItem("Notification", RouterPath.Sample.System.Notification))
        routerItems.add(RouterItem("Permission", RouterPath.Sample.System.Permission))
        routerItems.add(RouterItem("Typeface", RouterPath.Sample.System.Typeface))
        return routerItems
    }
}