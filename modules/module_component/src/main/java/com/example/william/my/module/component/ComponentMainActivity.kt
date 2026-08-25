package com.example.william.my.module.component

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Component.Main)
class ComponentMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("Broadcast", RouterPath.Component.Broadcast),
            RouterItem("Service", RouterPath.Component.Service),
            RouterItem("ActivityResult", RouterPath.Component.ActivityResult),
            RouterItem("OnBackPressed", RouterPath.Component.OnBackPressed)
        )
    }
}
