package com.example.william.my.module.okhttp.okhttp

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.utils.AppExecutorsHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

/**
 * OkHttp 基础示例
 *
 * 演示 OkHttp 原生 API 的基本用法，不依赖任何封装。
 * - FormBody.Builder：构建表单请求体
 * - JSON 请求体：application/json 格式
 * - MultipartBody.Builder：构建多部分请求体（文件上传场景）
 *
 * @see <a href="https://square.github.io/okhttp">OkHttp 官方文档</a>
 */
@Route(path = RouterPath.OkHttp.OkHttpLib.OkHttp)
class OkHttpActivity : BasicResponseActivity() {

    private val client: OkHttpClient = OkHttpClient()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("OkHttp 基础示例\n\n支持三种请求体格式：\n- FormBody（表单提交）\n- JSON（application/json）\n- MultipartBody（文件上传）\n\n点击下方按钮发起请求，日志会累积显示在上方")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "OkHttp Posting a FormBody",
            "OkHttp Posting a JSON Body",
            "OkHttp Posting a MultipartBody",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                AppExecutorsHelper.networkIO().execute {
                    postingForm(Constants.Value_Username, Constants.Value_Password)
                }
            }

            1 -> {
                AppExecutorsHelper.networkIO().execute {
                    postingJson(Constants.Value_Username, Constants.Value_Password)
                }
            }

            2 -> {
                AppExecutorsHelper.networkIO().execute {
                    postingMultipart(Constants.Value_Username, Constants.Value_Password)
                }
            }
        }
    }

    /**
     * FormBody：application/x-www-form-urlencoded 格式的 POST 请求。
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
     * JSON：application/json 格式的 POST 请求。
     */
    private fun postingJson(username: String, password: String) {
        val json = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(Constants.Url_Login)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("【JSON】失败：${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("【JSON】失败：$response")
                        return
                    }
                    appendFormatLog("【JSON】成功：", response.body.string())
                }
            }
        })
    }

    /**
     * MultipartBody：multipart/form-data 格式的 POST 请求（文件上传场景）。
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
