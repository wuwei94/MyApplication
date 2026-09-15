package com.example.william.my.module.http.activity.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttp.utils.JsonUtils
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit + RxJava — 原生回调式网络请求
 *
 * 使用 Retrofit.Builder 手动装配 Gson 转换器与 RxJava3 CallAdapter，
 * 将接口方法返回值桥接为 Single，由页面自行管理线程与订阅释放。
 *
 * 核心机制与避坑点：
 * 1. CallAdapter：RxJava3CallAdapterFactory 将 Call 桥接为 Single/Observable
 * 2. 线程控制：subscribeOn(io) + observeOn(main) 显式切换
 * 3. 订阅管理：CompositeDisposable 在 onDestroy 统一 dispose
 * 4. 原生装配：不依赖项目 DSL，便于对照 Retrofit 原生 API
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Http.RetrofitRx)
class RetrofitRxActivity : BasicResponseActivity() {

    private val operations = CompositeDisposable()

    override fun buildList(): ArrayList<String> = arrayListOf(
        "RetrofitRx login",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSingle(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSingle(username: String, password: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        val api: NetworkApi = retrofit.create(NetworkApi::class.java)
        val single: Single<RetrofitResponse<LoginData>> = api.loginSingle(username, password)

        operations.add(
            single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<RetrofitResponse<LoginData>>() {
                    override fun onSuccess(response: RetrofitResponse<LoginData>) {
                        appendFormatLog("Retrofit Rx 响应：", JsonUtils.toJson(response))
                    }

                    override fun onError(e: Throwable) {
                        appendLog("Retrofit Rx 失败：${e.message ?: "未知错误"}")
                    }
                }),
        )
    }

    override fun onDestroy() {
        operations.dispose()
        super.onDestroy()
    }
}
