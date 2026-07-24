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
            RouterItem("OkHttpHelperActivity", RouterPath.OkHttp.OkHttpLib.OkHttpHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitActivity", RouterPath.OkHttp.Retrofit.Retrofit)
        )
        routerItems.add(
            RouterItem("RetrofitHelperActivity", RouterPath.OkHttp.Retrofit.RetrofitHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitRxJavaActivity", RouterPath.OkHttp.Retrofit.RetrofitRxJava)
        )
        routerItems.add(
            RouterItem(
                "RetrofitRxJavaHelperActivity",
                RouterPath.OkHttp.Retrofit.RetrofitRxJavaHelper
            )
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RxRetrofitActivity", RouterPath.OkHttp.Retrofit.RxRetrofit)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("OkHttpDownloadActivity", RouterPath.OkHttp.Download.OkHttpDownload)
        )
        routerItems.add(
            RouterItem("RetrofitDownloadActivity", RouterPath.OkHttp.Download.RetrofitDownload)
        )
        routerItems.add(
            RouterItem("RxDownloadActivity", RouterPath.OkHttp.Download.RxDownload)
        )
        return routerItems
    }
}
