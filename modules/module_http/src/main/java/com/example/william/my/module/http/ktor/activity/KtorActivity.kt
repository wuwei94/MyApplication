package com.example.william.my.module.http.ktor.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Ktor — Kotlin 官方跨平台网络框架（原生 HttpClient）
 *
 * 核心机制与避坑点：
 * 1. Engine 抽象：本页用 OkHttp Engine，更换 Engine 时超时与插件行为可能不同
 * 2. 协程原生：请求是 suspend，须在协程作用域发起，禁止主线程阻塞等待
 * 3. 请求体形态：FormDataContent / 字符串 Body / MultiPartFormDataContent 分别对应 Form / JSON·Raw / Multipart
 * 4. 资源释放：HttpClient 持有连接池与线程，页面销毁前应 close，避免泄漏
 *
 * 官方参考：
 * https://ktor.io/
 */
@Route(path = RouterPath.Http.Ktor)
class KtorActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Ktor 示例：OkHttp Engine 发起 GET / Form / JSON / Raw / Multipart 与 Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求 (FormDataContent)",
        "3. 发送 POST JSON 请求",
        "4. 发送 POST Raw Body 请求",
        "5. 发送 POST Multipart 请求",
        "6. 配置 HttpClient（timeout + logging）后请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> ktorGet()
            1 -> ktorPostForm()
            2 -> ktorPostJson(Constants.Value_Username, Constants.Value_Password)
            3 -> ktorPostRaw(Constants.Value_Username, Constants.Value_Password)
            4 -> ktorPostMultipart()
            5 -> getWithConfiguredClient()
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

    private fun ktorGet() {
        appendLog("→ [GET] 发起请求...")
        lifecycleScope.launch {
            requestAndLog("[GET]") {
                ktorClient.get(Constants.Url_Article_List.replace("{page}", "0"))
            }
        }
    }

    private fun ktorPostForm() {
        appendLog("→ [Form] 发起请求...")
        lifecycleScope.launch {
            requestAndLog("[Form]") {
                ktorClient.post(Constants.Url_Login) {
                    setBody(
                        FormDataContent(
                            parameters {
                                append(Constants.Key_Username, Constants.Value_Username)
                                append(Constants.Key_Password, Constants.Value_Password)
                            },
                        ),
                    )
                }
            }
        }
    }

    private fun ktorPostJson(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val json = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()
        lifecycleScope.launch {
            requestAndLog("[JSON]") {
                ktorClient.post(Constants.Url_Login) {
                    contentType(ContentType.Application.Json)
                    setBody(json)
                }
            }
        }
    }

    private fun ktorPostRaw(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val raw = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
        lifecycleScope.launch {
            requestAndLog("[Raw]") {
                ktorClient.post(Constants.Url_Login) {
                    contentType(ContentType.Text.Plain)
                    setBody(raw)
                }
            }
        }
    }

    private fun ktorPostMultipart() {
        appendLog("→ [Multipart] 发起请求...")
        lifecycleScope.launch {
            requestAndLog("[Multipart]") {
                ktorClient.post(Constants.Url_Login) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(Constants.Key_Username, Constants.Value_Username)
                                append(Constants.Key_Password, Constants.Value_Password)
                            },
                        ),
                    )
                }
            }
        }
    }

    /**
     * 独立 HttpClient 展示 HttpTimeout + Logging 插件配置面，用完立即 close。
     */
    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 HttpClient 发起请求...")
        lifecycleScope.launch {
            val configured = HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 8000
                    connectTimeoutMillis = 8000
                    socketTimeoutMillis = 8000
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Utils.logcat(TAG, message)
                        }
                    }
                    level = LogLevel.HEADERS
                }
            }
            try {
                requestAndLog("[DSL]") {
                    configured.get(Constants.Url_Article_List.replace("{page}", "0"))
                }
            } finally {
                configured.close()
            }
        }
    }

    private suspend fun requestAndLog(tag: String, block: suspend () -> HttpResponse) {
        try {
            val response = block()
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                appendFormatLog("✓ $tag 响应：", body)
            } else {
                appendFormatLog("✗ $tag 失败（HTTP ${response.status.value}）：", body)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            appendLog("✗ $tag 失败：${error.message ?: "未知错误"}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ktorClient.close()
    }
}
