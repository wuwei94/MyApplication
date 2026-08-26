package com.example.william.my.module.http

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Http.Main)
class HttpMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        routerItems.add(RouterItem("── 基础客户端 ──", ""))
        routerItems.add(RouterItem("HttpURL", RouterPath.Http.HttpURL))
        routerItems.add(RouterItem("Volley", RouterPath.Http.Volley))
        routerItems.add(RouterItem("", ""))

        routerItems.add(RouterItem("── OkHttp ──", ""))
        routerItems.add(RouterItem("OkHttp", RouterPath.Http.OkHttp))
        routerItems.add(RouterItem("", ""))

        routerItems.add(RouterItem("── Retrofit ──", ""))
        routerItems.add(RouterItem("Retrofit Call", RouterPath.Http.RetrofitCall))
        routerItems.add(RouterItem("Retrofit Call (DSL)", RouterPath.Http.RetrofitCallDsl))
        routerItems.add(RouterItem("Retrofit Coroutine", RouterPath.Http.RetrofitCoroutine))
        routerItems.add(RouterItem("Retrofit Coroutine (DSL)", RouterPath.Http.RetrofitCoroutineDsl))
        routerItems.add(RouterItem("Retrofit Rx", RouterPath.Http.RetrofitRx))
        routerItems.add(RouterItem("Retrofit Rx (DSL)", RouterPath.Http.RetrofitRxDsl))
        routerItems.add(RouterItem("", ""))

        routerItems.add(RouterItem("── Rx 动态请求与文件传输 ──", ""))
        routerItems.add(RouterItem("RxRequest（动态请求）", RouterPath.Http.RxRequest))
        routerItems.add(RouterItem("RxDownload（文件下载）", RouterPath.Http.RxDownload))
        routerItems.add(RouterItem("RxUpload（文件上传）", RouterPath.Http.RxUpload))
        routerItems.add(RouterItem("", ""))

        routerItems.add(RouterItem("── Ktor ──", ""))
        routerItems.add(RouterItem("Ktor", RouterPath.Http.Ktor))
        routerItems.add(RouterItem("Ktor Client", RouterPath.Http.KtorClient))

        return routerItems
    }
}
