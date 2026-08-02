package com.example.william.my.module.okhttp.retrofit

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.UserData
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.retrofit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit

/**
 * 原生 Retrofit + RxJava 方式
 *
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.OkHttp.Retrofit.RetrofitRx)
class RetrofitRxActivity : BasicRecyclerActivity() {

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
        // 创建 Retrofit 实例
        val retrofit: Retrofit = Retrofit.Builder().build()

        // 创建网络请求接口实例
        val api: NetworkApi = retrofit.create(NetworkApi::class.java)

        // 调用网络接口中的方法获取 Single 对象
        val single: Single<RetrofitResponse<UserData?>> = api.loginSingle(username, password)

        // 进行网络请求
        single
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<RetrofitResponse<UserData?>>() {
                override fun onSuccess(response: RetrofitResponse<UserData?>) {
                    showResponse(response.string())
                }

                override fun onError(e: Throwable) {
                    showFailure(e.message)
                }
            })
    }
}
