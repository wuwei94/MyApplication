package com.example.william.my.module.http.activity.rxretrofit

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.retrofit
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
 * 2. 多种请求体：支持表单参数、JSONObject、Raw Body 与 Multipart 字段
 * 3. 生命周期绑定：setProvider(owner) 绑定宿主，销毁后自动释放
 * 4. 客户端配置：retrofit(instance) 可注入带 timeout/logging 的 OkHttp Client
 * 5. 统一回调：ResponseCallback 封装成功/失败分支与异常归一
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RxRequest)
class RxRequestActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "RxRequest 动态请求示例：GET / Form / JSON / Raw / Multipart / PUT 与 Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求",
        "3. 发送 POST JSON 请求",
        "4. 发送 POST Raw Body 请求",
        "5. 发送 POST Multipart 请求",
        "6. 发送 PUT 表单请求",
        "7. 配置 Client（timeout + logging）与 Header 后请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> get()
            1 -> postForm(Constants.Value_Username, Constants.Value_Password)
            2 -> postJson(Constants.Value_Username, Constants.Value_Password)
            3 -> postRaw(Constants.Value_Username, Constants.Value_Password)
            4 -> postMultipart(Constants.Value_Username, Constants.Value_Password)
            5 -> putForm(Constants.Value_Username, Constants.Value_Password)
            6 -> getWithConfiguredClient()
        }
    }

    private fun get() {
        appendLog("→ [GET] 发起请求...")
        RxRequest.builder<JsonElement>()
            .api(articleListUrl())
            .addParam("page", "0")
            .get()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("✓ [GET] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [GET] ${e.message}")
                }
            })
    }

    private fun postForm(username: String, password: String) {
        appendLog("→ [Post Form] 发起请求...")
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
                    appendFormatLog("✓ [Post Form] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [Post Form] ${e.message}")
                }
            })
    }

    private fun postJson(username: String, password: String) {
        appendLog("→ [Post JSON] 发起请求...")
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
                    appendFormatLog("✓ [Post JSON] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [Post JSON] ${e.message}")
                }
            })
    }

    private fun postRaw(username: String, password: String) {
        appendLog("→ [Post Raw] 发起请求...")
        val json = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()

        RxRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addJsonBody(json)
            .post()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("✓ [Raw Body] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [Raw Body] ${e.message}")
                }
            })
    }

    private fun postMultipart(username: String, password: String) {
        appendLog("→ [Multipart] 发起请求...")
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
                    appendFormatLog("✓ [Multipart] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [Multipart] ${e.message}")
                }
            })
    }

    private fun putForm(username: String, password: String) {
        appendLog("→ [Put Form] 发起请求...")
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        RxRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addParams(params)
            .put()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("✓ [Put Form] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [Put Form] ${e.message}")
                }
            })
    }

    /**
     * Builder.retrofit() 注入带 timeout/logging 的 Client，并叠加请求 Header 配置面。
     */
    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 配置 Client/ Header 后发起请求...")
        RxRequest.builder<JsonElement>()
            .api(articleListUrl())
            .addParam("page", "0")
            .get()
            .addHeader("X-Demo-Client", "rx-request-showcase")
            .retrofit(
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
            .setProvider(this)
            .buildSingle()
            .subscribe(object : ResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    appendFormatLog("✓ [DSL] ", response?.toString().orEmpty())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    appendLog("✗ [DSL] ${e.message}")
                }
            })
    }

    private fun articleListUrl(): String = Constants.Url_Article_List.replace("{page}", "0")
}
