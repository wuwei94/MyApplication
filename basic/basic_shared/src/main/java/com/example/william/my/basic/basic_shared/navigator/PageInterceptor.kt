package com.example.william.my.basic.basic_shared.navigator

import android.content.Context

/**
 * 页面跳转拦截器契约
 *
 * 按 [priority] 从高到低依次执行，可用于登录态校验、权限检查或路径重定向。
 */
interface PageInterceptor {

    /**
     * 拦截器优先级，数值越大越先执行
     */
    val priority: Int get() = 0

    /**
     * 执行拦截逻辑，需调用 [chain.proceed] 放行或重定向
     */
    fun intercept(chain: Chain)

    interface Chain {
        val context: Context
        val request: PageRequest

        fun proceed(request: PageRequest)
    }
}
