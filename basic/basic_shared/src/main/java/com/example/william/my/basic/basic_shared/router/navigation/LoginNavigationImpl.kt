package com.example.william.my.basic.basic_shared.router.navigation

import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 路由跳转日志工具
 */
class LoginNavigationImpl {

    private val TAG = "LoginNavigation"

    fun onArrived(path: String) {
        Utils.logcat(TAG, "跳转完成: $path")
    }
}
