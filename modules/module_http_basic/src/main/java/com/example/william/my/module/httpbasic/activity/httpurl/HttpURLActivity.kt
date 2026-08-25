package com.example.william.my.module.httpbasic.activity.httpurl

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.utils.AppExecutorsHelper
import com.example.william.my.core.httpurl.HttpURLUtils
import org.json.JSONObject

/**
 * HttpURLConnection — Android 原生网络请求
 *
 * HttpURLConnection 是 Android 原生的网络请求类，无需额外依赖。
 *
 * 核心特性：
 * 1. 原生支持：Android SDK 内置，无需额外依赖
 * 2. 轻量级：代码量少，适合简单请求
 * 3. 可定制：支持自定义请求头、超时设置等
 * 4. 线程管理：需要手动管理线程，回调在子线程
 *
 * 基本用法：
 * ```kotlin
 * // 创建连接
 * val url = URL("https://api.example.com/data")
 * val connection = url.openConnection() as HttpURLConnection
 *
 * // 设置请求方法
 * connection.requestMethod = "GET"
 *
 * // 设置超时
 * connection.connectTimeout = 5000
 * connection.readTimeout = 5000
 *
 * // 读取响应
 * val inputStream = connection.inputStream
 * val response = inputStream.bufferedReader().readText()
 *
 * // 关闭连接
 * connection.disconnect()
 * ```
 *
 * 适用场景：
 * - 简单的网络请求
 * - 不想引入第三方库
 * - 学习网络请求原理
 */
@Route(path = RouterPath.HttpBasic.HttpURL)
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
