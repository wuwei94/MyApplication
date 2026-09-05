package com.example.william.my.core.rx.request.function

import com.example.william.my.core.retrofit.exception.ServerResultException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.reactivex.rxjava3.functions.Function
import java.lang.reflect.Type

/**
 * 服务器返回结果，泛型转换(JsonElement -> 泛型)
 *
 * Api 接口统一返回 RetrofitResponse<JsonElement>，
 * 根据 RxRequestBuilder 捕获的 data 目标类型将 JsonElement 反序列化为 T。
 */
internal class ResponseFunction<T>(
    private val dataType: Type,
    private val gson: Gson = defaultGson,
) : Function<RetrofitResponse<JsonElement>, RetrofitResponse<T>> {

    override fun apply(response: RetrofitResponse<JsonElement>): RetrofitResponse<T> {
        if (!response.isSuccess) {
            throw ServerResultException(response.code, response.message)
        }
        val data = response.data?.let { gson.fromJson<T>(it, dataType) }
        return RetrofitResponse.of(response.code, response.message, data)
    }

    internal companion object {
        val defaultGson: Gson by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Gson() }
    }
}
