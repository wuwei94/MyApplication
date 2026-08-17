package com.example.william.my.basic.basic_shared.router.navigation

import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 路由跳转监听实现
 */
class LoginNavigationImpl : NavigationCallback {

    private val TAG = "LoginNavigation"

    override fun onFound(postcard: Postcard) {
        Utils.logcat(TAG, "找到了: " + postcard.path)
    }

    override fun onLost(postcard: Postcard) {
        Utils.logcat(TAG, "找不到了: " + postcard.path)
    }

    override fun onArrival(postcard: Postcard) {
        Utils.logcat(TAG, "跳转完了: " + postcard.path)
    }

    override fun onInterrupt(postcard: Postcard) {
        Utils.logcat(TAG, "被拦截了: " + postcard.path)
    }
}