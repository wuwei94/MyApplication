package com.example.william.my.module.http.retrofit.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.module.http.retrofit.data.RetrofitParallelApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 * Retrofit + RxJava（DSL）— 封装后订阅式网络请求
 *
 * 使用项目内 DSL 封装创建已安装 RxJava3 CallAdapter 的 Retrofit，
 * 通过 createRxApi 创建接口代理，在 CompositeDisposable 中管理订阅。
 *
 * 核心机制与避坑点：
 * 1. DSL 创建：rxRetrofit { } 一行得到已装配 CallAdapter 的 Retrofit 实例
 * 2. 类型安全 API：createRxApi 创建接口代理，编译期检查方法签名
 * 3. 线程控制：subscribeOn(io) + observeOn(main) 显式切换
 * 4. 请求体注解：@FormUrlEncoded / @Body / @Multipart 对应 Form / JSON·Raw / Multipart
 * 5. 订阅释放：CompositeDisposable 在 onDestroy 统一 dispose
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RetrofitRxDsl)
class RetrofitRxDslActivity : BasicResponseActivity() {

    private val operations = CompositeDisposable()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Retrofit Rx DSL 示例：GET / Form / JSON / Raw / Multipart 与 DSL Client 配置",
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
            0 -> getSingle()
            1 -> postFormSingle(Constants.Value_Username, Constants.Value_Password)
            2 -> postJsonSingle(Constants.Value_Username, Constants.Value_Password)
            3 -> postRawSingle(Constants.Value_Username, Constants.Value_Password)
            4 -> postMultipartSingle(Constants.Value_Username, Constants.Value_Password)
            5 -> getWithConfiguredClient()
        }
    }

    private fun defaultApi(): RetrofitParallelApi = createRxApi(
        RetrofitParallelApi::class.java,
        rxRetrofit { baseUrl(Constants.Url_Base) },
    )

    private fun getSingle() {
        appendLog("→ [GET] 发起请求...")
        subscribe(defaultApi().getSingle(0), "[GET]")
    }

    private fun postFormSingle(username: String, password: String) {
        appendLog("→ [Form] 发起请求...")
        subscribe(defaultApi().postFormSingle(username, password), "[Form]")
    }

    private fun postJsonSingle(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val body = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        subscribe(defaultApi().postJsonSingle(body), "[JSON]")
    }

    private fun postRawSingle(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val body = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
            .toRequestBody(RAW_MEDIA_TYPE)
        subscribe(defaultApi().postRawSingle(body), "[Raw]")
    }

    private fun postMultipartSingle(username: String, password: String) {
        appendLog("→ [Multipart] 发起请求...")
        subscribe(defaultApi().postMultipartSingle(username, password), "[Multipart]")
    }

    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        val api = createRxApi(
            RetrofitParallelApi::class.java,
            rxRetrofit {
                baseUrl(Constants.Url_Base)
                client(
                    okHttpClient {
                        timeout(15)
                        logging()
                    },
                )
            },
        )
        subscribe(api.getSingle(0), "[DSL]")
    }

    private fun subscribe(single: Single<ResponseBody>, tag: String) {
        operations.add(
            single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { body -> appendFormatLog("✓ $tag 响应：", body.string()) },
                    { error -> appendLog("✗ $tag 失败：${error.message ?: "未知错误"}") },
                ),
        )
    }

    override fun onDestroy() {
        operations.dispose()
        super.onDestroy()
    }

    private companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val RAW_MEDIA_TYPE = "text/plain; charset=utf-8".toMediaType()
    }
}
