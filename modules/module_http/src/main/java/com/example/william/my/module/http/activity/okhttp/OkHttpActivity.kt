package com.example.william.my.module.http.activity.okhttp

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.json.JSONObject

/**
 * OkHttp — 基础 HTTP 请求与多请求体形态演示
 *
 * 核心机制与避坑点：
 * 1. 同步与异步调用：[OkHttpClient.newCall] 提供 [Call.execute]（阻塞主线程）与 [Call.enqueue]（后台回调）；
 * 2. 响应流生命周期：[Response.body] 必须在 `use { ... }` 块内安全读取并释放，防止底层连接池泄漏；
 * 3. 请求体形态：[FormBody] 适用于 key-value 表单，JSON/Raw 使用 [toRequestBody] 指定 MediaType，
 *    [MultipartBody] 适用于文件与多媒体混合表单；
 * 4. DSL 配置面：`okHttpClient {}` 可配置 timeout / logging / cookieJar 等，每条链路应复用同一 Client 实例。
 *
 * 官方参考：
 * https://square.github.io/okhttp
 */
@Route(path = RouterPath.Http.OkHttp)
class OkHttpActivity : BasicResponseActivity() {

    /**
     * 使用 DSL 创建 OkHttpClient 实例。
     */
    private val client: OkHttpClient = okHttpClient {}

    /**
     * 演示 timeout / logging / cookieJar 组合配置的独立 Client。
     */
    private val configuredClient: OkHttpClient = okHttpClient {
        timeout(15)
        logging(HttpLoggingInterceptor.Level.BASIC)
        cookieJar()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "OkHttp 示例：GET / FormBody / JSON / Raw / MultipartBody 与 DSL Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送表单请求 (FormBody)",
        "3. 发送 JSON 请求 (RequestBody)",
        "4. 发送 Raw Body 请求 (RequestBody)",
        "5. 发送多部分表单请求 (MultipartBody)",
        "6. 使用 DSL 配置 Client（timeout + logging + cookieJar）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> getting()
            1 -> postingForm(Constants.Value_Username, Constants.Value_Password)
            2 -> postingJson(Constants.Value_Username, Constants.Value_Password)
            3 -> postingRaw(Constants.Value_Username, Constants.Value_Password)
            4 -> postingMultipart(Constants.Value_Username, Constants.Value_Password)
            5 -> getWithConfiguredClient()
        }
    }

    /**
     * 使用 Request.Builder 发起 GET 请求。
     *
     * 构建流程：Request.Builder → client.newCall().enqueue()
     */
    private fun getting() {
        appendLog("→ [GET] 发起请求...")
        val request = Request.Builder()
            .url(articleListUrl())
            .get()
            .build()

        enqueue(client, request, "[GET]")
    }

    /**
     * 使用 OkHttp 原生 FormBody.Builder 构建表单请求。
     *
     * 适用于 application/x-www-form-urlencoded 格式的 POST 请求。
     * 构建流程：FormBody.Builder → Request.Builder → client.newCall().enqueue()
     */
    private fun postingForm(username: String, password: String) {
        appendLog("→ [FormBody] 发起请求...")
        val requestBody = FormBody.Builder()
            .add(Constants.Key_Username, username)
            .add(Constants.Key_Password, password)
            .build()

        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        enqueue(client, request, "[FormBody]")
    }

    /**
     * 使用 application/json MediaType 的 RequestBody 提交 JSON 字符串。
     */
    private fun postingJson(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val json = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()

        val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        enqueue(client, request, "[JSON]")
    }

    /**
     * 使用无结构约束的 Raw Body 提交字节流，与 JSON/Form 的编码约定形成对照。
     */
    private fun postingRaw(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val raw = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
        val requestBody = raw.toRequestBody(RAW_MEDIA_TYPE)
        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        enqueue(client, request, "[Raw]")
    }

    /**
     * 使用 OkHttp 原生 MultipartBody.Builder 构建多部分请求。
     *
     * 适用于 multipart/form-data 格式的 POST 请求（文件上传等场景）。
     * 构建流程：MultipartBody.Builder → Request.Builder → client.newCall().enqueue()
     */
    private fun postingMultipart(username: String, password: String) {
        appendLog("→ [MultipartBody] 发起请求...")
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(Constants.Key_Username, username)
            .addFormDataPart(Constants.Key_Password, password)
            .build()

        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        enqueue(client, request, "[MultipartBody]")
    }

    /**
     * 使用带 timeout / logging / cookieJar 的 DSL Client 发起请求，观察配置面差异。
     */
    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        val request = Request.Builder()
            .url(articleListUrl())
            .get()
            .build()

        enqueue(configuredClient, request, "[DSL]")
    }

    private fun enqueue(target: OkHttpClient, request: Request, tag: String) {
        target.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("✗ $tag 请求失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("✗ $tag 响应异常：$response")
                        return
                    }
                    appendFormatLog("✓ $tag 请求成功：", response.body.string())
                }
            }
        })
    }

    private fun articleListUrl(): String = Constants.Url_Article_List.replace("{page}", "0")

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val RAW_MEDIA_TYPE = "text/plain; charset=utf-8".toMediaType()
    }
}
