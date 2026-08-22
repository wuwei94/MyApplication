package com.example.william.my.module.ktor.activity

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ktor.ktorClient
import com.example.william.my.core.ktor.request.postFormResponse
import com.example.william.my.core.okhttp.utils.JsonUtils
import kotlinx.coroutines.launch

/**
 * Ktor Client — 项目级 Ktor 客户端封装
 *
 * 演示固定使用 OkHttp Engine 的项目级 Ktor 客户端封装。
 *
 * 核心特性：
 * 1. 统一封装：项目级统一的网络请求封装
 * 2. OkHttp Engine：使用 OkHttp 作为底层引擎
 * 3. 协程支持：原生支持 Kotlin 协程
 * 4. 配置灵活：支持 baseUrl、timeout 等配置
 *
 * 基本用法：
 * ```kotlin
 * // 创建客户端
 * val client = ktorClient {
 *     baseUrl("https://api.example.com")
 *     timeout(15)
 * }
 *
 * // 发送请求
 * lifecycleScope.launch {
 *     val result = client.postFormResponse<LoginData>("user/login", params)
 *     result.onSuccess { response ->
 *         // 处理成功
 *     }
 *     result.onFailure { error ->
 *         // 处理失败
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 项目级网络请求封装
 * - 需要统一配置的场景
 * - Kotlin 协程项目
 */
@Route(path = RouterPath.Ktor.KtorClient)
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
