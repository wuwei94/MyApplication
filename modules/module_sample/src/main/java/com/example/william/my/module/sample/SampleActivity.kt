package com.example.william.my.module.sample

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_module.router.item.RouterItem
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Sample.Main)
class SampleActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AsyncTask", RouterPath.Sample.AsyncTask))
        routerItems.add(RouterItem("HandlerThread", RouterPath.Sample.HandlerThread))
        routerItems.add(RouterItem("JobScheduler", RouterPath.Sample.JobScheduler))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Broadcast", RouterPath.Sample.Broadcast))
        routerItems.add(RouterItem("Messenger", RouterPath.Sample.Messenger))
        routerItems.add(RouterItem("Service", RouterPath.Sample.Service))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("ActivityResult", RouterPath.Sample.ActivityResult))
        routerItems.add(RouterItem("OnBackPressed", RouterPath.Sample.OnBackPressed))
        routerItems.add(RouterItem("Notification", RouterPath.Sample.Notification))
        routerItems.add(RouterItem("Permission", RouterPath.Sample.Permission))
        routerItems.add(RouterItem("Typeface", RouterPath.Sample.Typeface))
        return routerItems
    }
}
