package com.example.william.my.module.http.activity.rxretrofit

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback
import com.example.william.my.core.rx.request.RxRequest
import com.google.gson.JsonElement
import org.json.JSONObject

/**
 * RxRequest — 基于 Retrofit + RxJava 的动态请求封装
 *
 * RxRequest 在 Retrofit + RxJava 之上提供 Builder 风格的动态请求能力，
 * 无需为每个接口预先声明方法即可发起表单、JSON 与 Multipart 请求。
 *
 * 核心机制与避坑点：
 * 1. 动态请求：通过 api(url) 运行时指定地址，不依赖预声明的 API 接口
 * 2. 多种请求体：支持表单参数、JSONObject 与 Multipart 字段
 * 3. 生命周期绑定：setProvider(owner) 绑定宿主，销毁后自动释放
 * 4. 统一回调：ResponseCallback 封装成功/失败分支与异常归一
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Http.RxRequest)
class RxRequestActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("RxRequest 动态请求示例（支持表单、JSON 与 Multipart）")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "RxRequest Post postForm",
        "RxRequest Post postJson",
        "RxRequest Post multipart",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                postForm(Constants.Value_Username, Constants.Value_Password)
            }

            1 -> {
                postJson(Constants.Value_Username, Constants.Value_Password)
            }

            2 -> {
                postMultipart(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun postForm(username: String, password: String) {
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        RxRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addParams(params)
            .post()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("Post Form 响应：", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("Post Form 失败：${e.message}")
                }
            })
    }

    private fun postJson(username: String, password: String) {
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        RxRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addJsonObject(jsonObject)
            .post()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("Post JSON 响应：", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("Post JSON 失败：${e.message}")
                }
            })
    }

    private fun postMultipart(username: String, password: String) {
        RxRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .post()
            .addMultipartFields(
                mapOf(
                    Constants.Key_Username to username,
                    Constants.Key_Password to password,
                ),
            )
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("Multipart 响应：", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("Multipart 失败：${e.message}")
                }
            })
    }
}
