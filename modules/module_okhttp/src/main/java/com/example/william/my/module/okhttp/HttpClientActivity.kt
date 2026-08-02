package com.example.william.my.module.okhttp

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.OkHttp.Main)
class HttpClientActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("OkHttpActivity", RouterPath.OkHttp.OkHttpLib.OkHttp)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitCall", RouterPath.OkHttp.Retrofit.RetrofitCall)
        )
        routerItems.add(
            RouterItem("RetrofitCallHelper", RouterPath.OkHttp.Retrofit.RetrofitCallHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitRx", RouterPath.OkHttp.Retrofit.RetrofitRx)
        )
        routerItems.add(
            RouterItem(
                "RetrofitRxHelper",
                RouterPath.OkHttp.Retrofit.RetrofitRxHelper
            )
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RxRetrofitActivity", RouterPath.OkHttp.Retrofit.RxRetrofit)
        )
        return routerItems
    }
}
