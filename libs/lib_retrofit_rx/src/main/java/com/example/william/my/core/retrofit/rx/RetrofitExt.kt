@file:JvmName("RetrofitExtKt")
@file:JvmMultifileClass

package com.example.william.my.core.retrofit.rx

import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 统一异常处理 + 线程切换（IO → MainThread）。
 *
 * Single → Single 转换，替代原 RetrofitHelper.buildSingle()。
 * 支持可空类型 RetrofitResponse<T?>
 */
fun <T : Any> Single<RetrofitResponse<T?>>.asNetwork(
): Single<RetrofitResponse<T?>> {
    return this
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

/**
 * 统一异常处理 + 线程切换（IO → MainThread）。
 *
 * 支持非空类型 RetrofitResponse<T>
 */
@JvmName("asNetworkNonNull")
fun <T : Any> Single<RetrofitResponse<T>>.asNetwork(
): Single<RetrofitResponse<T>> {
    return this
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

/**
 * 统一异常处理 + 线程切换 + 生命周期绑定。
 *
 * Activity/Fragment 销毁时自动取消订阅。
 * 支持可空类型 RetrofitResponse<T?>
 */
fun <T : Any> Single<RetrofitResponse<T?>>.asNetwork(
    owner: LifecycleOwner
): Single<RetrofitResponse<T?>> {
    return this
        .compose(AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle())
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

/**
 * 统一异常处理 + 线程切换 + 生命周期绑定。
 *
 * 支持非空类型 RetrofitResponse<T>
 */
@JvmName("asNetworkNonNull")
fun <T : Any> Single<RetrofitResponse<T>>.asNetwork(
    owner: LifecycleOwner
): Single<RetrofitResponse<T>> {
    return this
        .compose(AndroidLifecycle.createLifecycleProvider(owner).bindToLifecycle())
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
