package com.example.william.my.core.retrofit.rx.callback

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.observers.DisposableSingleObserver

/**
 * Rx 业务状态流回调
 *
 * 将加载、成功和失败统一转换为 [RetrofitResponse] 后交给 [onPostValue]。
 */
abstract class FlowCallback<T> :
    DisposableSingleObserver<RetrofitResponse<T>>(), RequestCallback<RetrofitResponse<T>> {

    override fun onSuccess(t: RetrofitResponse<T>) {
        onResponse(t)
    }

    override fun onError(e: Throwable) {
        if (e is ApiException) {
            onFailure(e)
        } else {
            onFailure(ApiException(e, ApiException.Error.UNKNOWN))
        }
    }

    override fun onLoading() {
        onPostValue(RetrofitResponse.loading())
    }

    override fun onToast(message: String?) {

    }

    override fun onResponse(response: RetrofitResponse<T>?) {
        onPostValue(response ?: RetrofitResponse.error("数据异常"))
    }

    override fun onFailure(e: ApiException) {
        onPostValue(RetrofitResponse.error(e.message))
    }

    open fun onPostValue(value: RetrofitResponse<T>) {

    }
}
