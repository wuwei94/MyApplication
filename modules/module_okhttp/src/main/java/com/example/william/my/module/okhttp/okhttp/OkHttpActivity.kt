package com.example.william.my.module.okhttp.okhttp

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
 * OkHttp — HTTP 客户端库
 *
 * OkHttp 是 Square 开源的 HTTP 客户端库，是 Android 最流行的网络请求库。
 *
 * 核心特性：
 * 1. HTTP/2 支持：支持 HTTP/2 协议，多路复用
 * 2. 连接池：自动管理连接池，减少延迟
 * 3. 透明压缩：支持 gzip 压缩，减少传输数据量
 * 4. 请求重试：自动重试失败的请求
 *
 * 请求体类型：
 * 1. FormBody：表单请求体（application/x-www-form-urlencoded）
 * 2. MultipartBody：多部分请求体（multipart/form-data），支持文件上传
 * 3. RequestBody：自定义请求体，支持 JSON 等格式
 *
 * 基本用法：
 * ```kotlin
 * // 创建客户端
 * val client = OkHttpClient()
 *
 * // 构建请求
 * val request = Request.Builder()
 *     .url("https://api.example.com/data")
 *     .post(formBody)
 *     .build()
 *
 * // 发送异步请求
 * client.newCall(request).enqueue(object : Callback {
 *     override fun onFailure(call: Call, e: IOException) {
 *         // 请求失败
 *     }
 *     override fun onResponse(call: Call, response: Response) {
 *         // 请求成功
 *     }
 * })
 * ```
 *
 * 适用场景：
 * - HTTP 请求
 * - 文件上传下载
 * - 需要高性能网络请求的场景
 *
 * https://square.github.io/okhttp
 */
@Route(path = RouterPath.OkHttp.OkHttp)
class OkHttpActivity : BasicResponseActivity() {

    /**
     * 创建 OkHttpClient
     */
    //private val client: OkHttpClient = OkHttpClient()

    /**
     * 使用 DSL 创建 OkHttpClient（无额外配置，使用默认值）。
     */
    private val client: OkHttpClient = okHttpClient {}

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("OkHttp 基础示例：支持 FormBody 与 MultipartBody 请求体")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "OkHttp Posting a FormBody",
            "OkHttp Posting a MultipartBody",
        )
    }

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
        // 1. 构建请求体（表单格式）
        val requestBody = FormBody.Builder()
            .add(Constants.Key_Username, username)
            .add(Constants.Key_Password, password)
            .build()

        // 2. 构建请求
        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        // 3. 发送异步请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("【FormBody】失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("【FormBody】失败：$response")
                        return
                    }
                    appendFormatLog("【FormBody】成功：", response.body.string())
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
        // 1. 构建请求体（多部分格式）
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(Constants.Key_Username, username)
            .addFormDataPart(Constants.Key_Password, password)
            .build()

        // 2. 构建请求
        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        // 3. 发送异步请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("【MultipartBody】失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("【MultipartBody】失败：$response")
                        return
                    }
                    appendFormatLog("【MultipartBody】成功：", response.body.string())
                }
            }
        })
    }
}
