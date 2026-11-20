package com.example.william.my.basic.basic_shared.router.interceptor

import com.example.william.my.basic.basic_shared.navigator.PageInterceptor
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 登录拦截器（基于自研 [PageInterceptor] 责任链）
 */
class LoginInterceptorImpl : PageInterceptor {

    private val TAG = "LoginInterceptor"

    override val priority: Int = 1

    override fun intercept(chain: PageInterceptor.Chain) {
        if (chain.request.extras.getInt("extra", 0) == RouterPath.PERMISSION_LOGIN) {
            Utils.logcat(TAG, "需要登录权限，拦截跳转: ${chain.request.path}")
            return
        }
        chain.proceed(chain.request)
    }
}
