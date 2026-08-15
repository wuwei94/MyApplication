package com.example.william.my.core.rx.download.callback

import com.example.william.my.core.retrofit.exception.ApiException

/**
 * Rx 下载业务回调。
 *
 * 单任务和批量任务分别使用自己的进度、结果模型，但共享相同的回调结构。
 */
abstract class RxDownloadCallback<P, R> {

    open fun onLoading() = Unit

    open fun onProgress(progress: P) = Unit

    open fun onResponse(response: R) = Unit

    /** 单任务失败，或下载队列本身无法继续执行时调用。队列内的任务失败记录在队列结果中。 */
    open fun onFailure(error: ApiException) = Unit
}
