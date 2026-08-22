package com.example.william.my.basic.basic_shared

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Module_Main)
class ModuleActivity : RouterRecyclerActivity() {

    @JvmField
    @Autowired(name = "param_key")
    var paramKey: String? = null

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Widget", RouterPath.Widget.Main))
        routerItems.add(RouterItem("Tab", RouterPath.Tab.Main))
        routerItems.add(RouterItem("Anim", RouterPath.Anim.Main))
        routerItems.add(RouterItem("CustomView", RouterPath.CustomView.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Async", RouterPath.Async.Main))
        routerItems.add(RouterItem("Component", RouterPath.Component.Main))
        routerItems.add(RouterItem("System", RouterPath.System.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Sample", RouterPath.Sample.Main))
        routerItems.add(RouterItem("Feature", RouterPath.Feature.Main))
        routerItems.add(RouterItem("Performance", RouterPath.Performance.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Http", RouterPath.Http.Main))
        routerItems.add(RouterItem("Ktor", RouterPath.Ktor.Main))
        routerItems.add(RouterItem("OkHttp", RouterPath.OkHttp.Main))
        routerItems.add(RouterItem("Retrofit", RouterPath.Retrofit.Main))
        routerItems.add(RouterItem("RxRetrofit", RouterPath.RxRetrofit.Main))
        routerItems.add(RouterItem("WebSocket", RouterPath.WebSocket.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Utils", RouterPath.Utils.Main))
        routerItems.add(RouterItem("Event", RouterPath.Event.Main))
        routerItems.add(RouterItem("OpenSource", RouterPath.OpenSource.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Kotlin", RouterPath.Kotlin.Main))
        routerItems.add(RouterItem("Jetpack", RouterPath.Jetpack.Main))
        routerItems.add(RouterItem("Database", RouterPath.Database.Main))
        routerItems.add(RouterItem("DI", RouterPath.DI.Main))
        routerItems.add(RouterItem("Arch", RouterPath.Arch.Main))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("Compose", RouterPath.Compose.Main))
        routerItems.add(RouterItem("Flutter", RouterPath.Flutter.Main))
        return routerItems
    }
}
