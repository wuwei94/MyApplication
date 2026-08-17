package com.example.william.my.module.okhttp.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * 原生 Retrofit + Call 回调方式
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.OkHttp.RetrofitCall)
class RetrofitCallActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Retrofit loginCall",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginCall(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginCall(username: String, password: String) {
        // 创建 Retrofit 实例
        val retrofit: Retrofit = Retrofit.Builder().build()

        // 创建网络请求接口实例
        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        // 调用网络接口中的方法获取 Call 对象
        val call: Call<ResponseBody> = api.loginCall(username, password)

        // 进行网络请求
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    appendFormatLog("Retrofit Call 响应：", response.body()?.string().orEmpty())
                } else {
                    appendFormatLog(
                        "Retrofit Call 失败（HTTP ${response.code()}）：",
                        response.errorBody()?.string().orEmpty()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                appendLog("Retrofit Call 失败：${t.message ?: "未知错误"}")
            }
        })
    }
}
