package com.example.william.my.module.http.volley.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.volley.VolleySingleton
import com.example.william.my.core.volley.builder.VolleyBuilder
import com.example.william.my.core.volley.listener.VolleyListener
import com.google.gson.JsonElement
import org.json.JSONObject

/**
 * Volley — Google 高并发小数据量网络请求框架
 *
 * 核心机制与避坑点：
 * 1. 主线程回调：Volley 内部切线程，Listener 回调已回到主线程，可直接更新 UI
 * 2. 请求队列：Request 须 enqueue 进队列；取消用 tag，勿只持引用置空
 * 3. 请求体形态：Builder 覆盖 Form / JSON；Raw / Multipart 通过自定义 RequestBody 写入队列
 * 4. 适用边界：适合高并发小数据量，大文件上传/下载应改用 OkHttp 传输链路
 *
 * 官方参考：
 * https://github.com/google/volley
 */
@Route(path = RouterPath.Http.Volley)
class VolleyActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Volley 示例：GET / Form / JSON / Raw / Multipart 与 Header/Tag 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求",
        "3. 发送 POST JSON 请求",
        "4. 发送 POST Raw Body 请求",
        "5. 发送 POST Multipart 请求",
        "6. 配置 Header 与 Tag 后请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> get()
            1 -> postForm(Constants.Value_Username, Constants.Value_Password)
            2 -> postJson(Constants.Value_Username, Constants.Value_Password)
            3 -> postRaw(Constants.Value_Username, Constants.Value_Password)
            4 -> postMultipart(Constants.Value_Username, Constants.Value_Password)
            5 -> getWithConfig()
        }
    }

    private fun get() {
        appendLog("→ [get] 发起请求...")
        VolleyBuilder<JsonElement>()
            .url(Constants.Url_Article_List.replace("{page}", "0"))
            .clazz(JsonElement::class.java)
            .get()
            .build(
                this,
                object : VolleyListener<JsonElement>() {
                    override fun onResponse(response: JsonElement?) {
                        appendLog("✓ [get] ${response?.toString()}")
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        appendLog("✗ [get] ${error?.message}")
                    }
                },
            )
    }

    private fun postForm(username: String, password: String) {
        appendLog("→ [postForm] 发起请求...")
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        VolleyBuilder<LoginData>()
            .url(Constants.Url_Login)
            .clazz(LoginData::class.java)
            .addParams(params)
            .post()
            .build(
                this,
                object : VolleyListener<LoginData>() {
                    override fun onResponse(response: LoginData?) {
                        appendLog("✓ [postForm] ${response?.let(JsonUtils::toJson)}")
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        appendLog("✗ [postForm] ${error?.message}")
                    }
                },
            )
    }

    private fun postJson(username: String, password: String) {
        appendLog("→ [postJson] 发起请求...")
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        VolleyBuilder<LoginData>()
            .url(Constants.Url_Login)
            .clazz(LoginData::class.java)
            .addJsonObject(jsonObject)
            .post()
            .build(
                this,
                object : VolleyListener<LoginData>() {
                    override fun onResponse(response: LoginData?) {
                        appendLog("✓ [postJson] ${response?.let(JsonUtils::toJson)}")
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        appendLog("✗ [postJson] ${error?.message}")
                    }
                },
            )
    }

    /**
     * 通过 StringRequest 自定义 Body 写入 Raw 字节流，对照 Form/JSON 的结构化编码。
     */
    private fun postRaw(username: String, password: String) {
        appendLog("→ [postRaw] 发起请求...")
        val raw = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
        enqueueStringRequest(
            tag = "[postRaw]",
            contentType = "text/plain; charset=utf-8",
            body = raw.toByteArray(),
        )
    }

    /**
     * 手动拼 multipart/form-data 边界，演示 Volley 队列内的 Multipart 写出方式。
     */
    private fun postMultipart(username: String, password: String) {
        appendLog("→ [postMultipart] 发起请求...")
        val boundary = "----VolleyBoundary${System.currentTimeMillis()}"
        val crlf = "\r\n"
        val payload = (
            "--$boundary$crlf" +
                "Content-Disposition: form-data; name=\"${Constants.Key_Username}\"$crlf$crlf" +
                "$username$crlf" +
                "--$boundary$crlf" +
                "Content-Disposition: form-data; name=\"${Constants.Key_Password}\"$crlf$crlf" +
                "$password$crlf" +
                "--$boundary--$crlf"
            ).toByteArray()
        enqueueStringRequest(
            tag = "[postMultipart]",
            contentType = "multipart/form-data; boundary=$boundary",
            body = payload,
        )
    }

    /**
     * VolleyBuilder 的 Header / Tag 配置面，用于请求标识与取消。
     */
    private fun getWithConfig() {
        appendLog("→ [config] 配置 Header/Tag 后发起请求...")
        VolleyBuilder<JsonElement>()
            .url(Constants.Url_Article_List.replace("{page}", "0"))
            .clazz(JsonElement::class.java)
            .addHeader("X-Demo-Client", "volley-showcase")
            .tag("volley-config")
            .get()
            .build(
                this,
                object : VolleyListener<JsonElement>() {
                    override fun onResponse(response: JsonElement?) {
                        appendLog("✓ [config] ${response?.toString()}")
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        appendLog("✗ [config] ${error?.message}")
                    }
                },
            )
    }

    private fun enqueueStringRequest(tag: String, contentType: String, body: ByteArray) {
        val request = object : StringRequest(
            Method.POST,
            Constants.Url_Login,
            { response -> appendLog("✓ $tag $response") },
            { error -> appendLog("✗ $tag ${error?.message}") },
        ) {
            override fun getBodyContentType(): String = contentType

            override fun getBody(): ByteArray = body
        }
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }
}
