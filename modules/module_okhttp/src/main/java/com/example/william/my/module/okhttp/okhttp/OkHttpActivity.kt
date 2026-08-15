package com.example.william.my.module.okhttp.okhttp

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
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
 * OkHttp 基础示例
 *
 * 演示 OkHttp 原生 API 的基本用法，不依赖任何封装。
 * 演示 Kotlin DSL 方式创建 OkHttpClient，以及原生 API 构建请求。
 * - okHttpClient {}：DSL 创建客户端
 * - FormBody.Builder：构建表单请求体
 * - JSON 请求体：application/json 格式
 * - MultipartBody.Builder：构建多部分请求体（文件上传场景）
 *
 * @see <a href="https://square.github.io/okhttp">OkHttp 官方文档</a>
 */
@Route(path = RouterPath.OkHttp.OkHttpLib.OkHttp)
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
