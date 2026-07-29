package com.example.william.my.core.retrofit.builder

import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.retrofit.converter.RetrofitConverterFactory
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

@DslMarker
annotation class RetrofitDslMarker

@RetrofitDslMarker
class RetrofitBuilder {

    private val builder = Retrofit.Builder()
    private var code: String = "errorCode"
    private var message: String = "errorMsg"
    private var converterFactory: Converter.Factory? = null

    init {
        // 默认使用全局兼容配置
        builder.baseUrl("http://host/")
        builder.client(okHttpClient { logging() })
    }

    // region 基础配置

    /** 设置 BaseUrl */
    fun baseUrl(url: String) {
        builder.baseUrl(url)
    }

    /** 设置 OkHttpClient */
    fun client(okHttpClient: OkHttpClient) {
        builder.client(okHttpClient)
    }

    // endregion

    // region 转换器与适配器

    /** 设置 Converter.Factory（覆盖默认的 RetrofitConverterFactory） */
    fun converter(factory: Converter.Factory) {
        converterFactory = factory
        builder.addConverterFactory(factory)
    }

    /** 设置 CallAdapter.Factory */
    fun callAdapter(factory: CallAdapter.Factory) {
        builder.addCallAdapterFactory(factory)
    }

    /** 设置响应码字段名（用于 RetrofitConverterFactory） */
    fun code(key: String) {
        code = key
    }

    /** 设置响应消息字段名（用于 RetrofitConverterFactory） */
    fun message(key: String) {
        message = key
    }

    // endregion

    // region 高级配置

    /** 直接操作底层 Retrofit.Builder */
    fun raw(block: Retrofit.Builder.() -> Unit) {
        builder.block()
    }

    // endregion

    internal fun build(): Retrofit {
        if (converterFactory == null) {
            builder.addConverterFactory(RetrofitConverterFactory.create(code, message))
        }
        return builder.build()
    }
}
