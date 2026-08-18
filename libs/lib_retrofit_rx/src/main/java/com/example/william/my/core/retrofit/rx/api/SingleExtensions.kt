@file:JvmName("SingleExtensions")
@file:JvmMultifileClass

package com.example.william.my.core.retrofit.rx.api

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.rx.function.ServerResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
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

/**
 * 将 RxJava3 的 [Single] 转换为生命周期感知的 [LiveData]。
 *
 * 遵循 ReactiveStreams 桥接机制：
 * 当 [LiveData] 处于活跃状态（Active）时自动订阅上游，处于非活跃状态（Inactive）时自动取消订阅（Dispose），
 * 避免内存泄漏与无效后台计算。
 */
fun <T : Any> Single<T>.toLiveData(): LiveData<T> {
    return object : MutableLiveData<T>() {
        private var disposable: Disposable? = null

        override fun onActive() {
            super.onActive()
            disposable = this@toLiveData.subscribe({ value ->
                postValue(value)
            }, { error ->
                // 异常处理：若上游已通过 withNetworkDefaults 转换为 RetrofitResponse，则不会抛出未经处理的未捕获异常
            })
        }

        override fun onInactive() {
            super.onInactive()
            disposable?.dispose()
            disposable = null
        }
    }
}
