package com.example.william.my.module.okhttp.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repository.api.NetworkApi
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.retrofit
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.OkHttp.Retrofit.Retrofit)
class RetrofitActivity : BasicRecyclerActivity() {

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
        // 使用 DSL 创建 Retrofit 实例
        val retrofit = retrofit {
            baseUrl(Constants.Url_Base)
        }

        // 创建网络请求接口实例
        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        // （4）调用网络接口中的方法获取 Call 对象
        val call: Call<ResponseBody> = api.loginCall(username, password)

        // （5）进行网络请求
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                showResponse(response.body()?.string())
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                showFailure(t.message)
            }
        })
    }
}
