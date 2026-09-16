package com.example.william.my.module.http.activity.retrofit

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.retrofit.rx.api.withNetworkDefaults
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback

/**
 * Retrofit + RxJava（DSL）— 封装后回调式网络请求
 *
 * 使用项目内 DSL 封装创建已安装 RxJava3 CallAdapter 的 Retrofit，
 * 并通过 withNetworkDefaults(owner) 统一异常处理、线程切换与生命周期绑定。
 *
 * 核心机制与避坑点：
 * 1. DSL 创建：rxRetrofit { } 一行得到可用 Retrofit 实例
 * 2. 类型安全 API：createRxApi 创建接口代理，编译期检查方法签名
 * 3. 统一默认项：withNetworkDefaults 完成线程切换与异常归一
 * 4. 生命周期感知：owner 销毁后自动释放订阅
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Http.RetrofitRxDsl)
class RetrofitRxDslActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Retrofit Rx DSL 示例：DSL 封装后订阅式登录请求")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. Retrofit Rx DSL 登录请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSingle(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSingle(username: String, password: String) {
        val retrofit = rxRetrofit { }
        val api = createRxApi(NetworkApi::class.java, retrofit)

        // withNetworkDefaults(owner) 统一异常处理、线程切换与生命周期绑定
        api.loginSingle(username, password)
            .withNetworkDefaults(this)
            .subscribe(object : ResponseCallback<LoginData>() {
                override fun onResponse(response: LoginData?) {
                    super.onResponse(response)
                    appendFormatLog(
                        "✓ Retrofit Rx DSL 响应：",
                        response?.let(JsonUtils::toJson).orEmpty(),
                    )
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ Retrofit Rx DSL 失败：${e.message}")
                }
            })
    }
}
