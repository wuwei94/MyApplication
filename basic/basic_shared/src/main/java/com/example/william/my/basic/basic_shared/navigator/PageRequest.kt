package com.example.william.my.basic.basic_shared.navigator

import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat

/**
 * 页面跳转请求模型
 *
 * 承载目标路由路径、Intent 参数、启动标志与转场动画选项，
 * 作为 [AppNavigator] 与 [PageInterceptor] 责任链之间的流转载体。
 */
data class PageRequest(
    var path: String,
    val extras: Bundle = Bundle(),
    var flags: Int = 0,
    var options: ActivityOptionsCompat? = null,
)
