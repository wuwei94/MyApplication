package com.example.william.my.module.network

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Network.Main)
class NetWorkActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(
            RouterItem("KtorActivity", RouterPath.Network.Ktor.Ktor)
        )
        routerItems.add(
            RouterItem("KtorUtilsActivity", RouterPath.Network.Ktor.KtorUtils)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("HttpURLActivity", RouterPath.Network.HttpURL.HttpURL)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("VolleyActivity", RouterPath.Network.Volley.Volley)
        )
        routerItems.add(
            RouterItem("VolleyHelperActivity", RouterPath.Network.Volley.VolleyHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("OkHttpActivity", RouterPath.Network.OkHttp.OkHttp)
        )
        routerItems.add(
            RouterItem("OkHttpHelperActivity", RouterPath.Network.OkHttp.OkHttpHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitActivity", RouterPath.Network.Retrofit.Retrofit)
        )
        routerItems.add(
            RouterItem("RetrofitHelperActivity", RouterPath.Network.Retrofit.RetrofitHelper)
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RetrofitRxJavaActivity", RouterPath.Network.Retrofit.RetrofitRxJava)
        )
        routerItems.add(
            RouterItem(
                "RetrofitRxJavaHelperActivity",
                RouterPath.Network.Retrofit.RetrofitRxJavaHelper
            )
        )

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("RxRetrofitActivity", RouterPath.Network.Retrofit.RxRetrofit)
        )

        //routerItems.add(
        //    RouterItem("", "")
        //)
        //routerItems.add(
        //    RouterItem("OkHttpDownloadActivity", RouterPath.Network.Download.OkHttpDownload)
        //)
        //routerItems.add(
        //    RouterItem("RetrofitDownloadActivity", RouterPath.Network.Download.RetrofitDownload)
        //)
        //routerItems.add(
        //    RouterItem("RxDownloadActivity", RouterPath.Network.Download.RxDownload)
        //)

        routerItems.add(
            RouterItem("", "")
        )
        routerItems.add(
            RouterItem("NanoActivity", RouterPath.Network.Socket.Nano)
        )
        return routerItems
    }
}
