package com.example.william.my.module.retrofit.retrofit_rx

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
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
 * 原生 Retrofit + RxJava 方式
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.Retrofit.RetrofitRx)
class RetrofitRxActivity : BasicResponseActivity() {

    private val operations = CompositeDisposable()

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "RetrofitRx login",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loginSingle(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun loginSingle(username: String, password: String) {
        // 创建带 RxJava3 CallAdapter 与 Gson 转换器的原生 Retrofit 实例
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.Url_Base)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        // 创建网络请求接口实例
        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        // 调用网络接口中的方法获取 Single 对象
        val single: Single<RetrofitResponse<LoginData>> = api.loginSingle(username, password)

        // 进行网络请求
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
                })
        )
    }

    override fun onDestroy() {
        operations.dispose()
        super.onDestroy()
    }
}
