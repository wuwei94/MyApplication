package com.example.william.my.basic.basic_shared.router.interceptor

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 登录拦截器
 */
@Interceptor(priority = 1)
class LoginInterceptorImpl : IInterceptor {

    private val TAG = "LoginInterceptor"

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        if (postcard.extra == RouterPath.PERMISSION_LOGIN) {
            Utils.logcat(TAG, "需要登录权限，拦截跳转: ${postcard.path}")
            callback.onInterrupt(RuntimeException("Need login"))
        } else {
            callback.onContinue(postcard)
        }
    }

    override fun init(context: Context) {}
}