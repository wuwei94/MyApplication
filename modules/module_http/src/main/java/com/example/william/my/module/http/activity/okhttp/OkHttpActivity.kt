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
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

/**
 * OkHttp — 基础 HTTP 请求与表单/多部分请求体演示
 *
 * 核心机制与避坑点：
 * 1. 同步与异步调用：[OkHttpClient.newCall] 提供 [Call.execute]（阻塞主线程）与 [Call.enqueue]（后台回调）；
 * 2. 响应流生命周期：[Response.body] 必须在 `use { ... }` 块内安全读取并释放，防止底层连接池泄漏；
 * 3. 请求体形态：[FormBody] 适用于 key-value 表单，[MultipartBody] 适用于文件与多媒体混合表单。
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

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("OkHttp 基础示例：支持 FormBody 与 MultipartBody 请求体")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送表单请求 (FormBody)",
        "2. 发送多部分表单请求 (MultipartBody)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> postingForm(Constants.Value_Username, Constants.Value_Password)
            1 -> postingMultipart(Constants.Value_Username, Constants.Value_Password)
        }
    }

    /**
     * 使用 OkHttp 原生 FormBody.Builder 构建表单请求。
     *
     * 适用于 application/x-www-form-urlencoded 格式的 POST 请求。
     * 构建流程：FormBody.Builder → Request.Builder → client.newCall().enqueue()
     */
    private fun postingForm(username: String, password: String) {
        val requestBody = FormBody.Builder()
            .add(Constants.Key_Username, username)
            .add(Constants.Key_Password, password)
            .build()

        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("✗ [FormBody] 请求失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("✗ [FormBody] 响应异常：$response")
                        return
                    }
                    appendFormatLog("✓ [FormBody] 请求成功：", response.body.string())
                }
            }
        })
    }

    /**
     * 使用 OkHttp 原生 MultipartBody.Builder 构建多部分请求。
     *
     * 适用于 multipart/form-data 格式的 POST 请求（文件上传等场景）。
     * 构建流程：MultipartBody.Builder → Request.Builder → client.newCall().enqueue()
     */
    private fun postingMultipart(username: String, password: String) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(Constants.Key_Username, username)
            .addFormDataPart(Constants.Key_Password, password)
            .build()

        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("✗ [MultipartBody] 请求失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("✗ [MultipartBody] 响应异常：$response")
                        return
                    }
                    appendFormatLog("✓ [MultipartBody] 请求成功：", response.body.string())
                }
            }
        })
    }
}
