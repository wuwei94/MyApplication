package com.example.william.my.core.rx.request

import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.rx.request.api.RequestApi
import com.example.william.my.core.rx.request.builder.RxRequestBuilder
import com.example.william.my.core.rx.request.config.RequestConfig
import com.example.william.my.core.rx.request.function.ResponseFunction
import com.example.william.my.core.rx.request.method.HttpMethod
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Type

/**
 * Rx 动态请求入口。
 */
class RxRequest<T> private constructor(private val config: RequestConfig) {

    internal constructor(builder: RxRequestBuilder<T>) : this(builder.buildConfig())

    private fun buildApi(): RequestApi = config.retrofit?.create(RequestApi::class.java)
        ?: createRxApi(RequestApi::class.java)

    internal fun createResponse(): Single<RetrofitResponse<T>> {
        val source =
            when (config.method) {
                HttpMethod.GET -> {
                    buildApi().get(config.api, config.header, config.parameter)
                }

                HttpMethod.POST -> {
                    if (config.multipartBody != null) {
                        buildApi().post(config.api, config.header, config.multipartBody)
                    } else if (config.requestBody != null) {
                        buildApi().post(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().post(config.api, config.header, config.parameter)
                    }
                }

                HttpMethod.PUT -> {
                    val body = config.multipartBody ?: config.requestBody
                    if (body != null) {
                        buildApi().put(config.api, config.header, body)
                    } else {
                        buildApi().put(config.api, config.header, config.parameter)
                    }
                }

                HttpMethod.PATCH -> {
                    val body = config.multipartBody ?: config.requestBody
                    if (body != null) {
                        buildApi().patch(config.api, config.header, body)
                    } else {
                        buildApi().patch(config.api, config.header, config.parameter)
                    }
                }

                HttpMethod.DELETE -> {
                    if (config.requestBody != null) {
                        buildApi().delete(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().delete(config.api, config.header, config.parameter)
                    }
                }
            }

        var response =
            source.map(ResponseFunction<T>(config.dataType))
                .onErrorResumeNext(HttpResultFunction())

        config.lifecycle?.let { lifecycle ->
            response = response.compose(lifecycle.bindToLifecycle())
        }

        response = response
            .subscribeOn(Schedulers.io())
            .observeOn(config.observeScheduler ?: AndroidSchedulers.mainThread())
        return response
    }

    companion object {
        inline fun <reified T> builder(): RxRequestBuilder<T> = RxRequestBuilder(object : TypeToken<T>() {}.type)

        @JvmStatic
        fun <T> builder(dataType: Type): RxRequestBuilder<T> = RxRequestBuilder(dataType)
    }
}
