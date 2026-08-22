package com.example.william.my.module.retrofit.retrofit_coroutine

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.retrofit.response.RetrofitResponse
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit + 协程 — 现代化网络请求
 *
 * 使用 Kotlin 协程的 Retrofit 网络请求方式，是 Google 推荐的方式。
 *
 * 核心特性：
 * 1. 协程支持：使用 suspend 函数，代码更简洁
 * 2. 生命周期感知：结合 lifecycleScope，自动取消请求
 * 3. 错误处理：使用 try-catch 处理异常
 * 4. 代码简洁：比回调方式更易读
 *
 * 基本用法：
 * ```kotlin
 * // 定义 API 接口
 * interface ApiService {
 *     @POST("login")
 *     suspend fun login(@Body body: LoginRequest): LoginResponse
 * }
 *
 * // 发起请求
 * lifecycleScope.launch {
 *     try {
 *         val response = api.login(request)
 *         // 处理成功
 *     } catch (e: Exception) {
 *         // 处理失败
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - Kotlin 协程项目
 * - 需要现代化网络请求方式
 * - 需要生命周期感知的场景
 *
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Retrofit.RetrofitCoroutine)
class RetrofitCoroutineActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Retrofit loginSuspend",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSuspend(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSuspend(username: String, password: String) {
        // 创建 Retrofit 实例
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 创建网络请求接口实例
        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        // 在生命周期协程作用域内发起挂起请求
        lifecycleScope.launch {
            try {
                val response: RetrofitResponse<LoginData> = api.loginSuspend(username, password)
                if (response.isSuccess) {
                    appendFormatLog("Retrofit Coroutine 响应：", JsonUtils.toJson(response))
                } else {
                    appendLog(
                        "Retrofit Coroutine 业务失败（${response.code}）：" +
                            response.message.ifBlank { "未知错误" }
                    )
                }
            } catch (e: Exception) {
                appendLog("Retrofit Coroutine 失败：${e.message ?: "未知错误"}")
            }
        }
    }
}
