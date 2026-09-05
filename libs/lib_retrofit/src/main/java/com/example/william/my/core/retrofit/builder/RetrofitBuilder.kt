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

    private var baseUrl: String = "http://host/"
    private var client: OkHttpClient? = null
    private var converterFactory: Converter.Factory? = null
    private var callAdapterFactory: CallAdapter.Factory? = null
    private var code: String = "errorCode"
    private var message: String = "errorMsg"
    private val rawBlocks = mutableListOf<Retrofit.Builder.() -> Unit>()

    // region 基础配置

    /** 设置 BaseUrl */
    fun baseUrl(url: String) {
        baseUrl = url
    }

    /** 设置 OkHttpClient */
    fun client(okHttpClient: OkHttpClient) {
        client = okHttpClient
    }

    // endregion

    // region 转换器与适配器

    /** 设置 Converter.Factory */
    fun converter(factory: Converter.Factory) {
        converterFactory = factory
    }

    /** 设置 CallAdapter.Factory */
    fun callAdapter(factory: CallAdapter.Factory) {
        callAdapterFactory = factory
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
        rawBlocks += block
    }

    // endregion

    internal fun build(): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client ?: okHttpClient { logging() })
            .addConverterFactory(
                converterFactory ?: RetrofitConverterFactory.create(code, message),
            )

        callAdapterFactory?.let {
            builder.addCallAdapterFactory(it)
        }

        rawBlocks.forEach { block ->
            builder.block()
        }

        return builder.build()
    }
}
