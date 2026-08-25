package com.example.william.my.module.retrofit.retrofit_coroutine

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
 * 封装后 Retrofit + 协程方式
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Retrofit.RetrofitCoroutineDsl)
class RetrofitCoroutineDslActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Retrofit DSL loginSuspend",
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
        val retrofit = retrofit { }

        // 创建网络请求接口实例
        val api = createApi(NetworkApi::class.java, retrofit)

        // 在生命周期协程作用域内发起挂起请求
        lifecycleScope.launch {
            try {
                val response: RetrofitResponse<LoginData> = api.loginSuspend(username, password)
                if (response.isSuccess) {
                    appendFormatLog("Retrofit Coroutine DSL 响应：", JsonUtils.toJson(response))
                } else {
                    appendLog(
                        "Retrofit Coroutine DSL 业务失败（${response.code}）：" +
                            response.message.ifBlank { "未知错误" }
                    )
                }
            } catch (e: Exception) {
                appendLog("Retrofit Coroutine DSL 失败：${e.message ?: "未知错误"}")
            }
        }
    }
}
