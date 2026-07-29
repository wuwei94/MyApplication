package com.example.william.my.core.retrofit.helper

import com.example.william.my.core.retrofit.cachedRetrofit
import com.example.william.my.core.retrofit.function.HttpResultFunction
import com.example.william.my.core.retrofit.function.ServerResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit

@Deprecated(
    "使用 RetrofitDsl（retrofit { } / cachedRetrofit { }）替代",
    ReplaceWith("retrofit { }", "com.example.william.my.core.retrofit.retrofit")
)
object RetrofitHelper {

    /**
     * 获取默认 Retrofit 实例（缓存在 "default" 名称下）。
     */
    fun retrofit(): Retrofit {
        return cachedRetrofit("default") {}
    }

    /**
     * 创建 API 接口实例。
     */
    fun <T> buildApi(api: Class<T>, retrofit: Retrofit = retrofit()): T {
        return retrofit.create(api)
    }

    /**
     * 包装 Single：业务异常转换 + 线程切换。
     */
    fun <T : Any> buildSingle(single: Single<RetrofitResponse<T?>>): Single<RetrofitResponse<T?>> {
        return single
            .map(ServerResultFunction())
            .onErrorResumeNext(HttpResultFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
