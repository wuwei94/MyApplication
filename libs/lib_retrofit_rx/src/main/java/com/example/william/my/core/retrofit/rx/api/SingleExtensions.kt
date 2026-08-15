@file:JvmName("SingleExtensions")
@file:JvmMultifileClass

package com.example.william.my.core.retrofit.rx.api

import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.rx.function.ServerResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 为网络请求应用默认 Rx 策略。
 *
 * 通过 [ServerResultFunction] 检查业务结果，再由 [HttpResultFunction] 转换上游异常，
 * 在 IO 线程订阅，并在主线程观察结果。
 * [RetrofitResponse] 的类型参数表示预期数据类型，数据是否存在由其可空的 `data` 属性表达。
 */
fun <T : Any> Single<RetrofitResponse<T>>.withNetworkDefaults(
): Single<RetrofitResponse<T>> {
    return this
        .map(ServerResultFunction<T>())
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

/**
 * 为网络请求应用默认 Rx 策略，并绑定生命周期。
 *
 * 通过 [ServerResultFunction] 检查业务结果，再由 [HttpResultFunction] 转换上游异常，
 * 在 IO 线程订阅，并在主线程观察结果；
 * 当 [owner] 的生命周期结束时自动取消订阅。
 * [RetrofitResponse] 的类型参数表示预期数据类型，数据是否存在由其可空的 `data` 属性表达。
 *
 * @param owner 用于管理订阅的生命周期持有者
 */
fun <T : Any> Single<RetrofitResponse<T>>.withNetworkDefaults(
    owner: LifecycleOwner
): Single<RetrofitResponse<T>> {
    return this
        .compose(AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle())
        .map(ServerResultFunction<T>())
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
