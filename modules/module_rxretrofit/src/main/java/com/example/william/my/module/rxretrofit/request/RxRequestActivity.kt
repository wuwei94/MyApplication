package com.example.william.my.module.rxretrofit.request

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback
import com.example.william.my.core.rx.request.RxRequest
import com.google.gson.JsonElement
import org.json.JSONObject

/**
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.RxRetrofit.Request)
class RxRequestActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("RxRequest 动态请求示例（支持表单、JSON 与 Multipart）")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "RxRequest Post postForm",
            "RxRequest Post postJson",
            "RxRequest Post multipart",
        )
    }

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
            Constants.Key_Password to password
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
                )
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
