package com.example.william.my.core.retrofit.rx.callback

import com.example.william.my.core.retrofit.exception.ApiException

/**
 * Rx 请求事件回调接口
 *
 * 统一加载、提示、响应和失败事件，供 `callback` 包内的 Rx 回调实现复用。
 */
interface RxCallback<T> {
    /**
     * onLoading
     */
    fun onLoading()

    /**
     * onToast
     *
     * @param message
     */
    fun onToast(message: String?)

    /**
     * onResponse
     *
     * @param response
     */
    fun onResponse(response: T?)

    /**
     * onFailure
     *
     * @param e
     */
    fun onFailure(e: ApiException)
}
