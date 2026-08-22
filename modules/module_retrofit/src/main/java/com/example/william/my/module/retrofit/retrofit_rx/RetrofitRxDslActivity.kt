package com.example.william.my.module.retrofit.retrofit_rx

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.retrofit.rx.api.withNetworkDefaults
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback

/**
 * 封装后 Retrofit + RxJava 方式（含生命周期绑定）
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Retrofit.RetrofitRxDsl)
class RetrofitRxDslActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Retrofit Rx DSL login",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSingle(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSingle(username: String, password: String) {
        // 创建已安装 RxJava3 CallAdapter 的 Retrofit 实例
        val retrofit = rxRetrofit { }

        // 创建 API 接口实例
        val api = createRxApi(NetworkApi::class.java, retrofit)

        // 调用接口方法，通过 withNetworkDefaults(owner) 统一异常处理、线程切换和生命周期绑定
        api.loginSingle(username, password)
            .withNetworkDefaults(this)
            .subscribe(object : ResponseCallback<LoginData>() {
                override fun onResponse(response: LoginData?) {
                    super.onResponse(response)
                    appendFormatLog(
                        "Retrofit Rx DSL 响应：",
                        response?.let(JsonUtils::toJson).orEmpty()
                    )
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("Retrofit Rx DSL 失败：${e.message}")
                }
            })
    }
}
