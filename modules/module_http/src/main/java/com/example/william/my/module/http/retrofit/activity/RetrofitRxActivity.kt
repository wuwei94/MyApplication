package com.example.william.my.module.http.retrofit.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.module.http.retrofit.data.RetrofitParallelApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

/**
 * Retrofit + RxJava — Rx 订阅式网络请求
 *
 * 使用 Retrofit.Builder 手动装配 RxJava3 CallAdapter，
 * 将接口方法返回值桥接为 Single，由页面自行管理线程与订阅释放。
 *
 * 核心机制与避坑点：
 * 1. CallAdapter：RxJava3CallAdapterFactory 将 Call 桥接为 Single/Observable
 * 2. 线程控制：subscribeOn(io) + observeOn(main) 显式切换
 * 3. 请求体注解：@FormUrlEncoded / @Body / @Multipart 对应 Form / JSON·Raw / Multipart
 * 4. 订阅管理：CompositeDisposable 在 onDestroy 统一 dispose
 * 5. 原生装配：不依赖项目 DSL，便于对照 Retrofit 原生 API
 *
 * 官方参考：
 * https://square.github.io/retrofit
 */
@Route(path = RouterPath.Http.RetrofitRx)
class RetrofitRxActivity : BasicResponseActivity() {

    private val operations = CompositeDisposable()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "Retrofit Rx 示例：GET / Form / JSON / Raw / Multipart 与 Client 配置",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发送 GET 请求",
        "2. 发送 POST 表单请求 (@FormUrlEncoded)",
        "3. 发送 POST JSON 请求 (@Body)",
        "4. 发送 POST Raw Body 请求 (@Body)",
        "5. 发送 POST Multipart 请求 (@Multipart)",
        "6. 配置 Client（timeout + logging）后请求",
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

    private fun createApi(retrofit: Retrofit): RetrofitParallelApi = retrofit.create(RetrofitParallelApi::class.java)

    private fun defaultRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.Url_Base)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    private fun getSingle() {
        appendLog("→ [GET] 发起请求...")
        subscribe(createApi(defaultRetrofit()).getSingle(0), "[GET]")
    }

    private fun postFormSingle(username: String, password: String) {
        appendLog("→ [Form] 发起请求...")
        subscribe(
            createApi(defaultRetrofit()).postFormSingle(username, password),
            "[Form]",
        )
    }

    private fun postJsonSingle(username: String, password: String) {
        appendLog("→ [JSON] 发起请求...")
        val body = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        subscribe(createApi(defaultRetrofit()).postJsonSingle(body), "[JSON]")
    }

    private fun postRawSingle(username: String, password: String) {
        appendLog("→ [Raw] 发起请求...")
        val body = "${Constants.Key_Username}=$username&${Constants.Key_Password}=$password"
            .toRequestBody(RAW_MEDIA_TYPE)
        subscribe(createApi(defaultRetrofit()).postRawSingle(body), "[Raw]")
    }

    private fun postMultipartSingle(username: String, password: String) {
        appendLog("→ [Multipart] 发起请求...")
        subscribe(
            createApi(defaultRetrofit()).postMultipartSingle(username, password),
            "[Multipart]",
        )
    }

    private fun getWithConfiguredClient() {
        appendLog("→ [DSL] 使用配置后的 Client 发起请求...")
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(
                okHttpClient {
                    timeout(15)
                    logging(HttpLoggingInterceptor.Level.BASIC)
                },
            )
            .build()
        subscribe(createApi(retrofit).getSingle(0), "[DSL]")
    }

    private fun subscribe(single: Single<ResponseBody>, tag: String) {
        operations.add(
            single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(response: ResponseBody) {
                        appendFormatLog("✓ $tag 响应：", response.string())
                    }

                    override fun onError(e: Throwable) {
                        appendLog("✗ $tag 失败：${e.message ?: "未知错误"}")
                    }
                }),
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
