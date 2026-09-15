package com.example.william.my.module.http.activity.retrofit

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.retrofit
import kotlinx.coroutines.launch

/**
 * Retrofit + 协程（DSL）— 封装后挂起式网络请求
 *
 * 使用项目内 DSL 创建 Retrofit 与 API 实例，
 * 在 lifecycleScope 中调用 suspend 接口方法，自动跟随页面生命周期取消。
 *
 * 核心机制与避坑点：
 * 1. DSL 创建：retrofit { } 得到带项目默认配置的 Retrofit
 * 2. 挂起接口：loginSuspend 等 suspend 方法天然异步
 * 3. 生命周期感知：lifecycleScope 在页面销毁时取消协程
 * 4. 异常处理：try-catch 统一捕获网络与业务失败
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Http.RetrofitCoroutineDsl)
class RetrofitCoroutineDslActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> = arrayListOf(
        "Retrofit DSL loginSuspend",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSuspend(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSuspend(username: String, password: String) {
        val retrofit = retrofit { }
        val api = createApi(NetworkApi::class.java, retrofit)

        lifecycleScope.launch {
            try {
                val response: RetrofitResponse<LoginData> = api.loginSuspend(username, password)
                if (response.isSuccess) {
                    appendFormatLog("Retrofit Coroutine DSL 响应：", JsonUtils.toJson(response))
                } else {
                    appendLog(
                        "Retrofit Coroutine DSL 业务失败（${response.code}）：" +
                            response.message.ifBlank { "未知错误" },
                    )
                }
            } catch (e: Exception) {
                appendLog("Retrofit Coroutine DSL 失败：${e.message ?: "未知错误"}")
            }
        }
    }
}
