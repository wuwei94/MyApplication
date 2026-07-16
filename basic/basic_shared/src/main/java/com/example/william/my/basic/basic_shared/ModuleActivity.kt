package com.example.william.my.basic.basic_shared

import android.os.Bundle
import android.os.Looper
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * Plugins:
 * GsonForMartPlus
 * Google library Version Querier
 * Alibaba Java Coding Guidelines
 * <p>
 * str：页面名_str_模块_描述
 * color：页面名_color_模块_描述
 * shape：模块名_页面名_color_模块_描述
 * drawable：模块名_页面名_color_模块_描述
 */
@Route(path = RouterPath.Module_Main)
class ModuleActivity : RouterRecyclerActivity() {

    @JvmField
    @Autowired(name = "param_key")
    var paramKey: String? = null

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("UI", RouterPath.UI.Main))
        routerItems.add(RouterItem("Tab", RouterPath.Tab.Main))
        routerItems.add(RouterItem("Anim", RouterPath.Anim.Main))
        routerItems.add(RouterItem("Widget", RouterPath.Widget.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Sync", RouterPath.Sync.Main))
        routerItems.add(RouterItem("Component", RouterPath.Component.Main))
        routerItems.add(RouterItem("System", RouterPath.System.Main))
        routerItems.add(RouterItem("Sample", RouterPath.Sample.Main))
        routerItems.add(RouterItem("Features", RouterPath.Features.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Utils", RouterPath.Utils.Main))
        routerItems.add(RouterItem("Network", RouterPath.Network.Main))
        routerItems.add(RouterItem("OpenSource", RouterPath.OpenSource.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Arch", RouterPath.Arch.Main))
        routerItems.add(RouterItem("Event", RouterPath.Event.Main))
        routerItems.add(RouterItem("Kotlin", RouterPath.Kotlin.Main))
        routerItems.add(RouterItem("JetPack", RouterPath.JetPack.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Compose", RouterPath.Compose.Main))
        routerItems.add(RouterItem("Flutter", RouterPath.Flutter.Main))
        return routerItems
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Looper.myQueue().addIdleHandler {
            println("addIdleHandler: queueIdle " + Thread.currentThread().name)
            false
        }
    }

    fun println(msg: String) {
        Utils.logcat(TAG, msg)
    }
}