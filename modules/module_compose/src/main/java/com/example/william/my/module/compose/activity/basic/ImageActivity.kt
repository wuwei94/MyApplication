package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.R

/**
 * Image — 图片显示
 *
 * 演示 Compose 中 Image 组件加载与显示图片资源。
 *
 * 核心特性：
 * 1. 加载位图与矢量资源
 * 2. 支持内容缩放与裁剪
 *
 * 适用场景：
 * - 图片展示
 * - 头像、图标
 *
 * https://developer.android.google.cn/jetpack/compose/graphics/images/loading
 */
@Route(path = RouterPath.Compose.Image)
class ImageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageExample()
        }
    }

    @Composable
    fun ImageExample() {
        Image(
            painter = painterResource(R.drawable.shared_ic_launcher),
            contentDescription = "",
        )
    }
}
