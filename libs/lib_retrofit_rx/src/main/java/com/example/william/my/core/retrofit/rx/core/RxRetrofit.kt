package com.example.william.my.core.retrofit.rx.core

import com.example.william.my.core.okhttp.media.MediaType
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.rx.api.Api
import com.example.william.my.core.retrofit.rx.builder.RequestBuilder
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.rx.function.RxRetrofitFunction
import com.example.william.my.core.retrofit.method.Method
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Http请求类
 */
class RxRetrofit<T>(private val builder: RequestBuilder<T>) {

    private fun buildApi(): Api {
        return createApi(Api::class.java)
    }

    fun createResponse(): Single<RetrofitResponse<T>> {
        val source =
            when (builder.method) {
                Method.GET -> {
                    buildApi().get(builder.api, builder.header, builder.parameter)
                }

                Method.POST -> {
                    if (builder.multipartBody != null) {
                        val requestBody = builder.multipartBody?.build()
                        buildApi().post(builder.api, builder.header, requestBody)
                    } else if (builder.jsonObject != null) {
                        val requestBody =
                            builder.jsonObject.toString()
                                .toRequestBody(MediaType.MEDIA_TYPE_JSON)
                        buildApi().post(builder.api, builder.header, requestBody)
                    } else {
                        buildApi().post(builder.api, builder.header, builder.parameter)
                    }
                }

                Method.PUT -> {
                    buildApi().put(builder.api, builder.header, builder.parameter)
                }

                Method.DELETE -> {
                    buildApi().delete(builder.api, builder.header, builder.parameter)
                }
            }

        var response =
            source.map(RxRetrofitFunction<T>()).onErrorResumeNext(HttpResultFunction())

        builder.lifecycle?.let { lifecycle ->
            response = response.compose(lifecycle.bindToLifecycle())
        }

        response = response
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        return response
    }

    companion object {
        fun <T> builder(): RequestBuilder<T> {
            return RequestBuilder()
        }
    }
}
