package com.example.william.my.core.retrofit.rx.function

import com.example.william.my.core.retrofit.exception.ExceptionHandler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleSource
import io.reactivex.rxjava3.functions.Function

/**
 * 统一异常管理
 */
class HttpResultFunction<T : Any> : Function<Throwable, SingleSource<T>> {

    override fun apply(t: Throwable): SingleSource<T> = Single.error(ExceptionHandler.handleException(t))
}
