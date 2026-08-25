package com.example.william.my.module.retrofit.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.retrofit
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Retrofit + Call — 封装后回调方式
 *
 * 使用 DSL 封装的 Retrofit + Call 回调方式，简化 Retrofit 配置。
 *
 * 核心特性：
 * 1. DSL 封装：使用 Kotlin DSL 简化 Retrofit 配置
 * 2. Call 回调：使用 Call.enqueue() 发起异步请求
 * 3. 类型安全：编译时检查 API 接口
 * 4. 代码简洁：比原生 Retrofit 更简洁
 *
 * 基本用法：
 * ```kotlin
 * // 创建 Retrofit 实例
 * val retrofit = retrofit { }
 *
 * // 创建 API 接口
 * val api = createApi(NetworkApi::class.java, retrofit)
 *
 * // 发起请求
 * val call = api.login(username, password)
 * call.enqueue(object : Callback<ResponseBody> {
 *     override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
 *         // 处理响应
 *     }
 *     override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
 *         // 处理失败
 *     }
 * })
 * ```
 *
 * 适用场景：
 * - 需要简化 Retrofit 配置的场景
 * - 喜欢 DSL 风格的开发者
 * - 需要 Call 回调方式的场景
 *
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Retrofit.RetrofitCallDsl)
class RetrofitCallDslActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Retrofit DSL loginCall",
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
        val retrofit = retrofit { }

        // 创建网络请求接口实例
        val api = createApi(NetworkApi::class.java, retrofit)

        // 调用网络接口中的方法获取 Call 对象
        val call: Call<ResponseBody> = api.loginCall(username, password)

        // 进行网络请求
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    appendFormatLog("Retrofit DSL 响应：", response.body()?.string().orEmpty())
                } else {
                    appendFormatLog(
                        "Retrofit DSL 失败（HTTP ${response.code()}）：",
                        response.errorBody()?.string().orEmpty()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                appendLog("Retrofit DSL 失败：${t.message ?: "未知错误"}")
            }
        })
    }
}
