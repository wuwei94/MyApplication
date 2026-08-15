package com.example.william.my.module.network.activity.ktor

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.ktor.ktorClient
import com.example.william.my.core.ktor.request.postFormResponse
import kotlinx.coroutines.launch

/** 演示固定使用 OkHttp Engine 的项目级 Ktor 客户端。 */
@Route(path = RouterPath.Network.Ktor.KtorClient)
class KtorClientActivity : BasicResponseActivity() {

    private val clientDelegate = lazy {
        ktorClient {
            baseUrl(Constants.Url_Base)
            timeout(15)
        }
    }
    private val client by clientDelegate

    override fun buildList(): ArrayList<String> {
        return arrayListOf("Ktor Client POST")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            ktorPost(Constants.Value_Username, Constants.Value_Password)
        }
    }

    private fun ktorPost(username: String, password: String) {
        val params = mapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password
        )

        lifecycleScope.launch {
            val result = client.postFormResponse<LoginData>("user/login", params)
            result.onSuccess { response ->
                if (response.isSuccess) {
                    appendFormatLog("Ktor Client 响应：", JsonUtils.toJson(response))
                } else {
                    appendLog(
                        "Ktor Client 业务失败（${response.code}）：" +
                            response.message.ifBlank { "未知错误" }
                    )
                }
            }
            result.onFailure { error ->
                appendLog("Ktor Client 失败：${error.message ?: "未知错误"}")
            }
        }
    }

    override fun onDestroy() {
        if (clientDelegate.isInitialized()) client.close()
        super.onDestroy()
    }
}
