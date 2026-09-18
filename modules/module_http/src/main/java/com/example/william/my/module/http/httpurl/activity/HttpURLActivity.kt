package com.example.william.my.module.http.httpurl.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ThreadUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.httpurl.HttpURLUtils
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * HttpURLConnection — Android 原生网络请求
 *
 * 核心机制与避坑点：
 * 1. 主线程限制：网络调用必须在子线程执行，本页使用 ThreadUtils 线程池发起请求
 * 2. 流式读写：响应流用完须 close/disconnect，避免连接无法归还；同一 InputStream 不可重复读取
 * 3. 连接复用：每次 openConnection 得到独立连接，长生命周期复用需自行维护
 * 4. 配置面：原生 API 无客户端 DSL，超时通过 connectTimeout / readTimeout 按连接设置
 *
 * 官方参考：
 * https://developer.android.com/reference/java/net/HttpURLConnection
 */
@Route(path = RouterPath.Http.HttpURL)
class HttpURLActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "HttpURLConnection 示例：GET / Form / JSON / Raw / Multipart 与连接超时配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求",
        "3. 发送 POST JSON 请求",
        "4. 发送 POST Raw Body 请求",
        "5. 发送 POST Multipart 请求",
        "6. 自定义连接超时发起 GET 请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                ThreadUtils.getIoPool().execute { get() }
            }

            1 -> {
                ThreadUtils.getIoPool().execute {
                    postForm(Constants.Value_Username, Constants.Value_Password)
                }
            }

            2 -> {
                ThreadUtils.getIoPool().execute {
                    postJson(Constants.Value_Username, Constants.Value_Password)
                }
            }

            3 -> {
                ThreadUtils.getIoPool().execute {
                    postRaw(Constants.Value_Username, Constants.Value_Password)
                }
            }

            4 -> {
                ThreadUtils.getIoPool().execute {
                    postMultipart(Constants.Value_Username, Constants.Value_Password)
                }
            }

            5 -> {
                ThreadUtils.getIoPool().execute { getWithCustomTimeout() }
            }
        }
    }

    private fun get() {
        appendLog("→ [get] 发起请求...")
        val url = Constants.Url_Article_List.replace("{page}", "0")

        HttpURLUtils.get(
            url,
            listener = {
                appendLog("✓ [get] $it")
            },
            errorListener = {
                appendLog("✗ [get] ${it?.message}")
            },
        )
    }

    private fun postForm(username: String, password: String) {
        appendLog("→ [postForm] 发起请求...")
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password,
        )

        HttpURLUtils.postForm(
            Constants.Url_Login,
            params,
            listener = {
                appendLog("✓ [postForm] $it")
            },
            errorListener = {
                appendLog("✗ [postForm] ${it?.message}")
            },
        )
    }

    private fun postJson(username: String, password: String) {
        appendLog("→ [postJson] 发起请求...")
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        HttpURLUtils.postJson(
            Constants.Url_Login,
            jsonObject,
            listener = {
                appendLog("✓ [postJson] $it")
            },
            errorListener = {
                appendLog("✗ [postJson] ${it?.message}")
            },
        )
    }

    /**
     * HttpURLConnection 直接写出无表单/JSON 编码约定的 Raw Body。
     */
    private fun postRaw(username: String, password: String) {
        appendLog("→ [postRaw] 发起请求...")
        val raw = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
        val result = executeConnection(
            method = "POST",
            contentType = "text/plain; charset=utf-8",
            body = raw.toByteArray(),
        )
        logConnectionResult("[postRaw]", result)
    }

    /**
     * 手动拼 multipart/form-data 边界并写出，对应 FormBody/MultipartBody 的原生对照轴。
     */
    private fun postMultipart(username: String, password: String) {
        appendLog("→ [postMultipart] 发起请求...")
        val boundary = "----HttpURLBoundary${System.currentTimeMillis()}"
        val crlf = "\r\n"
        val payload = buildString {
            append("--$boundary$crlf")
            append("Content-Disposition: form-data; name=\"${Constants.Key_Username}\"$crlf$crlf")
            append(username)
            append(crlf)
            append("--$boundary$crlf")
            append("Content-Disposition: form-data; name=\"${Constants.Key_Password}\"$crlf$crlf")
            append(password)
            append(crlf)
            append("--$boundary--$crlf")
        }
        val result = executeConnection(
            method = "POST",
            contentType = "multipart/form-data; boundary=$boundary",
            body = payload.toByteArray(),
        )
        logConnectionResult("[postMultipart]", result)
    }

    /**
     * 通过 HttpURLUtils 暴露的 connectTimeout / readTimeout 展示原生配置面。
     */
    private fun getWithCustomTimeout() {
        appendLog("→ [get+timeout] 发起请求（connect/read = 8s）...")
        val url = Constants.Url_Article_List.replace("{page}", "0")
        HttpURLUtils.get(
            url,
            listener = {
                appendLog("✓ [get+timeout] $it")
            },
            errorListener = {
                appendLog("✗ [get+timeout] ${it?.message}")
            },
            connectTimeout = 8000,
            readTimeout = 8000,
        )
    }

    private fun executeConnection(
        method: String,
        contentType: String?,
        body: ByteArray?,
    ): ConnectionResult {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(Constants.Url_Login).openConnection() as HttpURLConnection
            conn.requestMethod = method
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            contentType?.let { conn.setRequestProperty("Content-Type", it) }
            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { os ->
                    os.write(body)
                    os.flush()
                }
            }
            val code = conn.responseCode
            val response = StringBuilder()
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            stream?.bufferedReader()?.use { reader ->
                reader.forEachLine { response.append(it).append('\n') }
            }
            ConnectionResult(code, response.toString(), null)
        } catch (error: Exception) {
            ConnectionResult(null, "", error)
        } finally {
            conn?.disconnect()
        }
    }

    private fun logConnectionResult(tag: String, result: ConnectionResult) {
        when {
            result.error != null -> appendLog("✗ $tag ${result.error.message}")
            result.code in 200..299 -> appendLog("✓ $tag ${result.body}")
            else -> appendLog("✗ $tag HTTP ${result.code} ${result.body}")
        }
    }

    private data class ConnectionResult(
        val code: Int?,
        val body: String,
        val error: Exception?,
    )
}
