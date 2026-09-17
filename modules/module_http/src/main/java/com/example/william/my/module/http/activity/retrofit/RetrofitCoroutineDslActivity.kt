package com.example.william.my.module.http.activity.retrofit

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.retrofit
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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
 * 4. 请求体注解：@FormUrlEncoded / @Body / @Multipart 对应 Form / JSON·Raw / Multipart
 * 5. 异常处理：try-catch 统一捕获网络与业务失败
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RetrofitCoroutineDsl)
class RetrofitCoroutineDslActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Retrofit 协程 DSL 示例：GET / Form / JSON / Raw / Multipart 与 DSL Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求 (@FormUrlEncoded)",
        "3. 发送 POST JSON 请求 (@Body)",
        "4. 发送 POST Raw Body 请求 (@Body)",
        "5. 发送 POST Multipart 请求 (@Multipart)",
        "6. 使用 DSL 配置 Client（timeout + logging）后请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> getSuspend()
            1 -> postFormSuspend(Constants.Value_Username, Constants.Value_Password)
            2 -> postJsonSuspend(Constants.Value_Username, Constants.Value_Password)
            3 -> postRawSuspend(Constants.Value_Username, Constants.Value_Password)
            4 -> postMultipartSuspend(Constants.Value_Username, Constants.Value_Password)
            5 -> getWithConfiguredClient()
        }
    }

    private fun defaultApi(): RetrofitParallelApi = createApi(
        RetrofitParallelApi::class.java,
        retrofit { baseUrl(Constants.Url_Base) },
    )

    private fun getSuspend() {
        appendLog("→ [GET] 发起请求...")
        launchSuspend("[GET]") { api -> api.getSuspend(0).string() }
    }

    private fun postFormSuspend(username: String, password: String) {
        appendLog("→ [Form] 发起请求...")
        launchSuspend("[Form]") { api -> api.postFormSuspend(username, password).string() }
    }

    private fun postJsonSuspend(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val body = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        launchSuspend("[JSON]") { api -> api.postJsonSuspend(body).string() }
    }

    private fun postRawSuspend(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val body = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
            .toRequestBody(RAW_MEDIA_TYPE)
        launchSuspend("[Raw]") { api -> api.postRawSuspend(body).string() }
    }

    private fun postMultipartSuspend(username: String, password: String) {
        appendLog("→ [Multipart] 发起请求...")
        launchSuspend("[Multipart]") { api ->
            api.postMultipartSuspend(username, password).string()
        }
    }

    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        val api = createApi(
            RetrofitParallelApi::class.java,
            retrofit {
                baseUrl(Constants.Url_Base)
                client(
                    okHttpClient {
                        timeout(15)
                        logging()
                    },
                )
            },
        )
        lifecycleScope.launch {
            try {
                val body = api.getSuspend(0).string()
                appendFormatLog("✓ [DSL] 响应：", body)
            } catch (e: Exception) {
                appendLog("✗ [DSL] 失败：${e.message ?: "未知错误"}")
            }
        }
    }

    private fun launchSuspend(
        tag: String,
        block: suspend (RetrofitParallelApi) -> String,
    ) {
        val api = defaultApi()
        lifecycleScope.launch {
            try {
                val body: String = block(api)
                appendFormatLog("✓ $tag 响应：", body)
            } catch (e: Exception) {
                appendLog("✗ $tag 失败：${e.message ?: "未知错误"}")
            }
        }
    }

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val RAW_MEDIA_TYPE = "text/plain; charset=utf-8".toMediaType()
    }
}
