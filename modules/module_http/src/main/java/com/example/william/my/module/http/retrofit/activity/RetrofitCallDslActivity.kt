package com.example.william.my.module.http.retrofit.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.retrofit
import com.example.william.my.module.http.retrofit.data.RetrofitParallelApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Retrofit + Call — 项目 DSL 封装的回调方式
 *
 * 核心机制与避坑点：
 * 1. DSL 装配：retrofit {} + createApi 提供项目默认 Converter / Client 配置
 * 2. 异步回调：enqueue 仍在 OkHttp 线程回调，UI 更新须切主线程
 * 3. 请求体注解：@FormUrlEncoded / @Body / @Multipart 分别对应 Form / JSON·Raw / Multipart
 * 4. 对照原生：与 RetrofitCallActivity 平行，便于对照手写 Builder 的配置差异
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RetrofitCallDsl)
class RetrofitCallDslActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Retrofit Call DSL 示例：GET / Form / JSON / Raw / Multipart 与 DSL Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求 (@FormUrlEncoded)",
        "3. 发送 POST JSON 请求 (@Body)",
        "4. 发送 POST Raw Body 请求 (@Body)",
        "5. 发送 POST Multipart 请求 (@Multipart)",
        "6. 使用 DSL 配置 Client（timeout + logging）后请求",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> getCall()
            1 -> postFormCall(Constants.Value_Username, Constants.Value_Password)
            2 -> postJsonCall(Constants.Value_Username, Constants.Value_Password)
            3 -> postRawCall(Constants.Value_Username, Constants.Value_Password)
            4 -> postMultipartCall(Constants.Value_Username, Constants.Value_Password)
            5 -> getWithConfiguredClient()
        }
    }

    private fun defaultApi(): RetrofitParallelApi = createApi(
        RetrofitParallelApi::class.java,
        retrofit { baseUrl(Constants.Url_Base) },
    )

    private fun getCall() {
        appendLog("→ [GET] 发起请求...")
        enqueue(defaultApi().getCall(0), "[GET]")
    }

    private fun postFormCall(username: String, password: String) {
        appendLog("→ [Form] 发起请求...")
        enqueue(defaultApi().postFormCall(username, password), "[Form]")
    }

    private fun postJsonCall(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val body = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        enqueue(defaultApi().postJsonCall(body), "[JSON]")
    }

    private fun postRawCall(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val body = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
            .toRequestBody(RAW_MEDIA_TYPE)
        enqueue(defaultApi().postRawCall(body), "[Raw]")
    }

    private fun postMultipartCall(username: String, password: String) {
        appendLog("→ [Multipart] 发起请求...")
        enqueue(defaultApi().postMultipartCall(username, password), "[Multipart]")
    }

    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        val api = createApi(
            RetrofitParallelApi::class.java,
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
        enqueue(api.getCall(0), "[DSL]")
    }

    private fun enqueue(call: Call<ResponseBody>, tag: String) {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    appendFormatLog("✓ $tag 响应：", response.body()?.string().orEmpty())
                } else {
                    appendFormatLog(
                        "✗ $tag 失败（HTTP ${response.code()}）：",
                        response.errorBody()?.string().orEmpty(),
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                appendLog("✗ $tag 失败：${t.message ?: "未知错误"}")
            }
        })
    }

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val RAW_MEDIA_TYPE = "text/plain; charset=utf-8".toMediaType()
    }
}
