package com.example.william.my.core.okhttp.compat

import okhttp3.Interceptor
import okhttp3.OkHttpClient

object CompatInterceptor {

    /**
     * 添加应用拦截器，在请求发送前和响应返回后各调用一次。
     */
    fun addInterceptor(builder: OkHttpClient.Builder, interceptor: Interceptor) {
        builder.addInterceptor(interceptor)
    }

    /**
     * 添加网络拦截器，在每次网络请求时调用（含重定向）。
     */
    fun addNetworkInterceptor(builder: OkHttpClient.Builder, interceptor: Interceptor) {
        builder.addNetworkInterceptor(interceptor)
    }
}
