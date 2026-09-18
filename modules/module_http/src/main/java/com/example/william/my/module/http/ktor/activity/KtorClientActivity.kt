package com.example.william.my.module.http.ktor.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ktor.ktorClient
import com.example.william.my.core.ktor.request.getResponse
import com.example.william.my.core.ktor.request.postFormResponse
import com.example.william.my.core.ktor.request.putFormResponse
import com.example.william.my.core.ktor.request.requestBodyResponse
import com.example.william.my.core.ktor.response.KtorResponse
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.google.gson.JsonElement
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Ktor Client — 项目级 Ktor 客户端封装（OkHttp Engine）
 *
 * 核心机制与避坑点：
 * 1. 封装入口：ktorClient {} 产出带 baseUrl / timeout 的 HttpClient，调用方负责 close
 * 2. Engine 固定：底层绑定 OkHttp Engine，插件与拦截行为随 OkHttp 配置变化
 * 3. 生命周期：本页用 lazy 延迟创建，页面销毁时须释放，防止连接池驻留
 * 4. 成对 API：getResponse / postFormResponse / requestBodyResponse 对齐信封与裸数据
 *
 * 官方参考：
 * https://ktor.io/
 */
@Route(path = RouterPath.Http.KtorClient)
class KtorClientActivity : BasicResponseActivity() {

    private val clientDelegate = lazy {
        ktorClient {
            baseUrl(Constants.Url_Base)
            timeout(15)
        }
    }
    private val client by clientDelegate

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Ktor Client 示例：GET / Form / JSON / Raw / Multipart / PUT Form 与 DSL 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求",
        "3. 发送 POST JSON 请求",
        "4. 发送 POST Raw Body 请求",
        "5. 发送 POST Multipart 请求",
        "6. 发送 PUT 表单请求",
        "7. 使用 DSL 配置 Client（baseUrl + timeout + logging）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> ktorGet()
            1 -> ktorPostForm(Constants.Value_Username, Constants.Value_Password)
            2 -> ktorPostJson(Constants.Value_Username, Constants.Value_Password)
            3 -> ktorPostRaw(Constants.Value_Username, Constants.Value_Password)
            4 -> ktorPostMultipart(Constants.Value_Username, Constants.Value_Password)
            5 -> ktorPutForm(Constants.Value_Username, Constants.Value_Password)
            6 -> getWithConfiguredClient()
        }
    }

    private fun ktorGet() {
        appendLog("→ [GET] 发起请求...")
        lifecycleScope.launch {
            val result = client.getResponse<JsonElement>("article/list/0/json")
            logResult("[GET]", result)
        }
    }

    private fun ktorPostForm(username: String, password: String) {
        appendLog("→ [POST Form] 发起请求...")
        val params = mapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        lifecycleScope.launch {
            val result = client.postFormResponse<LoginData>("user/login", params)
            logResult("[POST Form]", result)
        }
    }

    private fun ktorPostJson(username: String, password: String) {
        appendLog("→ [POST JSON] 发起请求...")
        val body = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()

        lifecycleScope.launch {
            val result = client.requestBodyResponse<LoginData>(
                url = "user/login",
                method = HttpMethod.Post,
                body = body,
                mediaType = ContentType.Application.Json,
            )
            logResult("[POST JSON]", result)
        }
    }

    private fun ktorPostRaw(username: String, password: String) {
        appendLog("→ [POST Raw] 发起请求...")
        val body = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"

        lifecycleScope.launch {
            val result = client.requestBodyResponse<LoginData>(
                url = "user/login",
                method = HttpMethod.Post,
                body = body,
                mediaType = ContentType.Text.Plain,
            )
            logResult("[POST Raw]", result)
        }
    }

    private fun ktorPostMultipart(username: String, password: String) {
        appendLog("→ [POST Multipart] 发起请求...")
        lifecycleScope.launch {
            val result = client.requestBodyResponse<LoginData>(
                url = "user/login",
                method = HttpMethod.Post,
                body = MultiPartFormDataContent(
                    formData {
                        append(Constants.Key_Username, username)
                        append(Constants.Key_Password, password)
                    },
                ),
            )
            logResult("[POST Multipart]", result)
        }
    }

    private fun ktorPutForm(username: String, password: String) {
        appendLog("→ [PUT Form] 发起请求...")
        val params = mapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        lifecycleScope.launch {
            val result = client.putFormResponse<LoginData>("user/login", params)
            logResult("[PUT Form]", result)
        }
    }

    /**
     * 通过 ktorClient DSL 组合 baseUrl / timeout / logging，请求结束后 close 释放连接池。
     */
    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        lifecycleScope.launch {
            val configured = ktorClient {
                baseUrl(Constants.Url_Base)
                timeout(8)
                logging()
            }
            try {
                val result = configured.getResponse<JsonElement>("article/list/0/json")
                logResult("[DSL GET]", result)
            } finally {
                configured.close()
            }
        }
    }

    private fun <T> logResult(tag: String, result: Result<KtorResponse<T>>) {
        result.onSuccess { response ->
            if (response.isSuccess) {
                appendFormatLog("✓ $tag 响应：", JsonUtils.toJson(response))
            } else {
                appendLog(
                    "✗ $tag 业务失败（${response.code}）：" +
                        response.message.ifBlank { "未知错误" },
                )
            }
        }
        result.onFailure { error ->
            appendLog("✗ $tag 失败：${error.message ?: "未知错误"}")
        }
    }

    override fun onDestroy() {
        if (clientDelegate.isInitialized()) client.close()
        super.onDestroy()
    }
}
