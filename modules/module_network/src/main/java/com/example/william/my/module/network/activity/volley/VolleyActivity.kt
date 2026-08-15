package com.example.william.my.module.network.activity.volley

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.volley.VolleyError
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.volley.builder.VolleyBuilder
import com.example.william.my.core.volley.listener.VolleyListener
import org.json.JSONObject

/**
 * Volley 示例。
 *
 * 核心特性：
 * - RequestQueue 请求队列，自动管理线程和缓存
 * - VolleyBuilder 链式构建请求：url() → clazz() → post() → build()
 * - 自动线程管理，回调在主线程执行
 * - 支持 GET/POST/PUT/DELETE，支持 Form 和 JSON 请求体
 */
@Route(path = RouterPath.Network.Volley.Volley)
class VolleyActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Volley 示例：支持 POST Form 与 POST JSON")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Volley postForm（表单提交）",
            "Volley postJson（JSON 提交）",
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
        }
    }

    private fun postForm(username: String, password: String) {
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password
        )

        VolleyBuilder<LoginData>()
            .url(Constants.Url_Login)
            .clazz(LoginData::class.java)
            .addParams(params)
            .post()
            .build(this, object : VolleyListener<LoginData>() {
                override fun onResponse(response: LoginData?) {
                    appendLog("【postForm】成功：${response?.let(JsonUtils::toJson)}")
                }

                override fun onErrorResponse(error: VolleyError?) {
                    appendLog("【postForm】失败：${error?.message}")
                }
            })
    }

    private fun postJson(username: String, password: String) {
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        VolleyBuilder<LoginData>()
            .url(Constants.Url_Login)
            .clazz(LoginData::class.java)
            .addJsonObject(jsonObject)
            .post()
            .build(this, object : VolleyListener<LoginData>() {
                override fun onResponse(response: LoginData?) {
                    appendLog("【postJson】成功：${response?.let(JsonUtils::toJson)}")
                }

                override fun onErrorResponse(error: VolleyError?) {
                    appendLog("【postJson】失败：${error?.message}")
                }
            })
    }
}
