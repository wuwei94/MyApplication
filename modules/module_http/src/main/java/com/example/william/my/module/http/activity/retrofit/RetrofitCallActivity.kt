package com.example.william.my.module.http.activity.retrofit

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Retrofit + Call — 原生回调式网络请求
 *
 * 使用 Retrofit.Builder 构建 API 接口实例，通过 Call.enqueue() 发起异步请求。
 *
 * 核心机制与避坑点：
 * 1. 原生回调：Call.enqueue / execute，无需额外依赖
 * 2. 同步异步：同时支持 execute 与 enqueue 两种调用形态
 * 3. 线程自理：回调线程与主线程切换需调用方自行处理
 *
 * 注意：response.body()?.string() 只能调用一次，后续访问会返回 null。
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Http.RetrofitCall)
class RetrofitCallActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Retrofit Call 示例：原生 enqueue 回调发起登录请求")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. Retrofit Call 登录请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginCall(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginCall(username: String, password: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .build()

        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        val call: Call<ResponseBody> = api.loginCall(username, password)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    appendFormatLog("✓ Retrofit Call 响应：", response.body()?.string().orEmpty())
                } else {
                    appendFormatLog(
                        "✗ Retrofit Call 失败（HTTP ${response.code()}）：",
                        response.errorBody()?.string().orEmpty(),
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                appendLog("✗ Retrofit Call 失败：${t.message ?: "未知错误"}")
            }
        })
    }
}
