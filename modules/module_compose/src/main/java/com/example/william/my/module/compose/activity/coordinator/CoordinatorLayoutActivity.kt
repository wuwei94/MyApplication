package com.example.william.my.module.compose.activity.coordinator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * CoordinatorLayout — 协调布局
 *
 * 演示 CoordinatorLayout 的滚动联动与嵌套滚动行为。
 *
 * 核心特性：
 * 1. 视图间滚动联动
 * 2. 支持折叠与吸顶效果
 *
 * 适用场景：
 * - 折叠头部
 * - 滚动视差效果
 *
 * https://developer.android.google.cn/reference/androidx/coordinatorlayout/widget/CoordinatorLayout
 */
@Route(path = RouterPath.Compose.CoordinatorLayout)
class CoordinatorLayoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                println(innerPadding)
                MyCoordinatorLayout()
            }
        }
    }

    @Composable
    fun MyCoordinatorLayout() {
    }
}
