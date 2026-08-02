package com.example.william.my.core.retrofit.rx.function

import com.example.william.my.core.retrofit.exception.ServerResultException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.google.gson.JsonElement
import io.reactivex.rxjava3.functions.Function

/**
 * 服务器返回结果，泛型转换(JsonElement -> 泛型)
 *
 * Api 接口统一返回 RetrofitResponse<JsonElement>，
 * 此处通过类型擦除转换为调用方期望的 RetrofitResponse<T>。
 * 调用方通常使用 JsonElement 作为 T，再自行反序列化。
 */
class RxRetrofitFunction<T> :
    Function<RetrofitResponse<JsonElement>, RetrofitResponse<T>> {

    @Suppress("UNCHECKED_CAST")
    override fun apply(response: RetrofitResponse<JsonElement>): RetrofitResponse<T> {
        //抛出服务器返回自定义异常
        if (!response.isSuccess) {
            throw ServerResultException(response.code, response.message)
        }
        // 安全：JVM 泛型擦除，RetrofitResponse<JsonElement> 与 RetrofitResponse<T> 运行时相同
        return response as RetrofitResponse<T>
    }
}