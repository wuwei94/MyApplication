package com.example.william.my.module.network.activity.httpurl

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.utils.AppExecutorsHelper
import com.example.william.my.core.httpurl.HttpURLUtils
import org.json.JSONObject

/**
 * HttpURLConnection 示例。
 *
 * 核心特性：
 * - setDoOutput() 默认 false，POST 请求必须设为 true
 * - setDoInput() 默认 true，用于读取响应
 * - setUseCaches() 设置缓存，POST 请求不能使用缓存
 * - connectTimeout / readTimeout 控制连接和读取超时
 * - disconnect() 释放连接资源
 */
@Route(path = RouterPath.Network.HttpURL)
class HttpURLActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("HttpURLConnection 示例：支持 POST Form 与 POST JSON")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "HttpURL postForm（表单提交）",
            "HttpURL postJson（JSON 提交）",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                AppExecutorsHelper.networkIO().execute {
                    postForm(Constants.Value_Username, Constants.Value_Password)
                }
            }

            1 -> {
                AppExecutorsHelper.networkIO().execute {
                    postJson(Constants.Value_Username, Constants.Value_Password)
                }
            }
        }
    }

    private fun postForm(username: String, password: String) {
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password
        )

        HttpURLUtils.postForm(
            Constants.Url_Login, params,
            listener = {
                appendLog("【postForm】成功：$it")
            },
            errorListener = {
                appendLog("【postForm】失败：${it?.message}")
            })
    }

    private fun postJson(username: String, password: String) {
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        HttpURLUtils.postJson(
            Constants.Url_Login, jsonObject,
            listener = {
                appendLog("【postJson】成功：$it")
            },
            errorListener = {
                appendLog("【postJson】失败：${it?.message}")
            })
    }
}
