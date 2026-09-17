package com.example.william.my.module.http.activity.retrofit

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit

/**
 * Retrofit + 协程 — 现代化网络请求
 *
 * 核心机制与避坑点：
 * 1. 挂起接口：suspend 方法在协程中调用，禁止在主线程 runBlocking 等待
 * 2. 生命周期：lifecycleScope 在 DESTROYED 时取消请求，避免回调泄漏
 * 3. 请求体注解：@FormUrlEncoded / @Body / @Multipart 分别对应 Form / JSON·Raw / Multipart
 * 4. 异常边界：网络异常与业务错误码分开处理，勿用同一 catch 吞掉业务失败
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RetrofitCoroutine)
class RetrofitCoroutineActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Retrofit 协程示例：GET / Form / JSON / Raw / Multipart 与 Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求 (@FormUrlEncoded)",
        "3. 发送 POST JSON 请求 (@Body)",
        "4. 发送 POST Raw Body 请求 (@Body)",
        "5. 发送 POST Multipart 请求 (@Multipart)",
        "6. 配置 Client（timeout + logging）后请求",
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

    private fun createApi(retrofit: Retrofit): RetrofitParallelApi = retrofit.create(RetrofitParallelApi::class.java)

    private fun defaultRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.Url_Base)
        .build()

    private fun getSuspend() {
        appendLog("→ [GET] 发起请求...")
        launchSuspend("[GET]") { api -> api.getSuspend(0).string() }
    }

    private fun postFormSuspend(username: String, password: String) {
        appendLog("→ [Form] 发起请求...")
        launchSuspend("[Form]") { api ->
            api.postFormSuspend(username, password).string()
        }
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
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .client(
                okHttpClient {
                    timeout(15)
                    logging(HttpLoggingInterceptor.Level.BASIC)
                },
            )
            .build()
        launchSuspend("[DSL]", retrofit) { api -> api.getSuspend(0).string() }
    }

    private fun launchSuspend(
        tag: String,
        retrofit: Retrofit = defaultRetrofit(),
        block: suspend (RetrofitParallelApi) -> String,
    ) {
        val api = createApi(retrofit)
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
