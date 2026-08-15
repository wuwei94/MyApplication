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
            RouterItem("RetrofitCallDsl", RouterPath.OkHttp.Retrofit.RetrofitCallDsl)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitRx", RouterPath.OkHttp.RetrofitRx.RetrofitRx)
        )
        routerItems.add(
            RouterItem("RetrofitRxDsl", RouterPath.OkHttp.RetrofitRx.RetrofitRxDsl)
        )

        return routerItems
    }
}
