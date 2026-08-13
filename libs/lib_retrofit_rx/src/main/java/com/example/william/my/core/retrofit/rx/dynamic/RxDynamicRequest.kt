package com.example.william.my.core.retrofit.rx.dynamic

import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.function.RxHttpResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Type

/**
 * Http请求类
 */
class RxDynamicRequest<T> private constructor(private val config: RxDynamicRequestConfig) {

    constructor(builder: RxDynamicRequestBuilder<T>) : this(builder.buildConfig())

    private fun buildApi(): RxDynamicRequestApi {
        return config.retrofit?.create(RxDynamicRequestApi::class.java)
            ?: createRxApi(RxDynamicRequestApi::class.java)
    }

    fun createResponse(): Single<RetrofitResponse<T>> {
        val source =
            when (config.method) {
                RxDynamicHttpMethod.GET -> {
                    buildApi().get(config.api, config.header, config.parameter)
                }

                RxDynamicHttpMethod.POST -> {
                    if (config.multipartBody != null) {
                        buildApi().post(config.api, config.header, config.multipartBody)
                    } else if (config.requestBody != null) {
                        buildApi().post(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().post(config.api, config.header, config.parameter)
                    }
                }

                RxDynamicHttpMethod.PUT -> {
                    if (config.requestBody != null) {
                        buildApi().put(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().put(config.api, config.header, config.parameter)
                    }
                }

                RxDynamicHttpMethod.PATCH -> {
                    if (config.requestBody != null) {
                        buildApi().patch(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().patch(config.api, config.header, config.parameter)
                    }
                }

                RxDynamicHttpMethod.DELETE -> {
                    if (config.requestBody != null) {
                        buildApi().delete(config.api, config.header, config.requestBody)
                    } else {
                        buildApi().delete(config.api, config.header, config.parameter)
                    }
                }
            }

        var response =
            source.map(RxDynamicResponseFunction<T>(config.dataType))
                .onErrorResumeNext(RxHttpResultFunction())

        config.lifecycle?.let { lifecycle ->
            response = response.compose(lifecycle.bindToLifecycle())
        }

        response = response
            .subscribeOn(Schedulers.io())
            .observeOn(config.observeScheduler ?: AndroidSchedulers.mainThread())
        return response
    }

    companion object {
        inline fun <reified T> builder(): RxDynamicRequestBuilder<T> {
            return RxDynamicRequestBuilder(object : TypeToken<T>() {}.type)
        }

        @JvmStatic
        fun <T> builder(dataType: Type): RxDynamicRequestBuilder<T> {
            return RxDynamicRequestBuilder(dataType)
        }
    }
}
