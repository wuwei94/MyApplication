package com.example.william.my.core.retrofit.rx.callback

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.observers.DisposableSingleObserver

/**
 * Rx 业务响应回调
 *
 * 从 [RetrofitResponse] 中提取业务数据，并统一分发成功与失败结果。
 */
abstract class ResponseCallback<T> :
    DisposableSingleObserver<RetrofitResponse<T>>(), RequestCallback<T> {

    override fun onSuccess(t: RetrofitResponse<T>) {
        onToast(t.message)
        onResponse(t.data)
    }

    override fun onError(e: Throwable) {
        if (e is ApiException) {
            onFailure(e)
        } else {
            onFailure(ApiException(e, ApiException.Error.UNKNOWN))
        }
    }

    override fun onLoading() {

    }

    override fun onToast(message: String?) {

    }

    override fun onResponse(response: T?) {

    }

    override fun onFailure(e: ApiException) {

    }
}
