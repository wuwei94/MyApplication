package com.example.william.my.core.rx.upload.callback

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.example.william.my.core.rx.upload.model.UploadResult

/**
 * Rx 上传业务回调。
 */
abstract class RxUploadCallback {

    open fun onLoading() = Unit

    open fun onProgress(progress: UploadProgress) = Unit

    open fun onResponse(response: UploadResult) = Unit

    open fun onFailure(error: ApiException) = Unit
}
