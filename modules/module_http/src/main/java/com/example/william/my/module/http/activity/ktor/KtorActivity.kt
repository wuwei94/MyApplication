package com.example.william.my.module.http.activity.ktor

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Ktor — Kotlin 官方现代跨平台网络框架
 *
 * 核心特性：
 * 1. Kotlin 优先：原生支持 Kotlin 协程，异步编程友好
 * 2. 多平台支持：支持 Android、iOS、Web、Desktop 等平台 (KMP)
 * 3. 插件系统：通过插件扩展功能（日志、认证、序列化等）
 * 4. 轻量级：依赖少，APK 体积小
 *
 * https://ktor.io/
 */
@Route(path = RouterPath.Http.Ktor)
class KtorActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "ktor post",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                ktorPost()
            }
        }
    }

    private val ktorClient = HttpClient(OkHttp) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Utils.logcat(TAG, message)
                }
            }

            level = LogLevel.ALL
        }
    }

    private fun ktorPost() {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = ktorClient.post(Constants.Url_Login) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(Constants.Key_Username, Constants.Value_Username)
                                append(Constants.Key_Password, Constants.Value_Password)
                            })
                    )
                }
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    appendFormatLog("Ktor 响应：", body)
                } else {
                    appendFormatLog("Ktor 失败（HTTP ${response.status.value}）：", body)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                appendLog("Ktor 失败：${error.message ?: "未知错误"}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ktorClient.close()
    }
}
