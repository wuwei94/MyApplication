package com.example.william.my.core.retrofit.rx.callback

import com.example.william.my.core.retrofit.exception.ApiException
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import okhttp3.ResponseBody
import java.io.InputStream

/**
 * Rx 文件响应回调
 *
 * 将 [ResponseBody] 转换为输入流，并统一分发成功与失败结果。
 */
abstract class RxFileCallback :
    DisposableSingleObserver<ResponseBody>(), RxCallback<InputStream> {

    override fun onSuccess(t: ResponseBody) {
        onResponse(t.byteStream())
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

    override fun onResponse(response: InputStream?) {

    }

    override fun onFailure(e: ApiException) {

    }
}
