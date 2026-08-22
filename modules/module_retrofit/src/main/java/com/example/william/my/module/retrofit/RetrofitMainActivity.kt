package com.example.william.my.module.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Retrofit.Main)
class RetrofitMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("RetrofitCall", RouterPath.Retrofit.RetrofitCall)
        )
        routerItems.add(
            RouterItem("RetrofitCallDsl", RouterPath.Retrofit.RetrofitCallDsl)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitCoroutine", RouterPath.Retrofit.RetrofitCoroutine)
        )
        routerItems.add(
            RouterItem("RetrofitCoroutineDsl", RouterPath.Retrofit.RetrofitCoroutineDsl)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitRx", RouterPath.Retrofit.RetrofitRx)
        )
        routerItems.add(
            RouterItem("RetrofitRxDsl", RouterPath.Retrofit.RetrofitRxDsl)
        )

        return routerItems
    }
}
