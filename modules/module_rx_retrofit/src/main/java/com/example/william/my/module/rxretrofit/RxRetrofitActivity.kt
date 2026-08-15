package com.example.william.my.module.rxretrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Rx 动态请求与文件传输示例入口
 */
@Route(path = RouterPath.RxRetrofit.Main)
class RxRetrofitActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("RxRequest", RouterPath.RxRetrofit.Request),
            RouterItem("RxDownload", RouterPath.RxRetrofit.Download),
            RouterItem("RxUpload", RouterPath.RxRetrofit.Upload),
        )
    }
}
