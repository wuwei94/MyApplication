package com.example.william.my.module.compose.activity.swipeable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * AnchoredDraggable — 锚点拖拽
 *
 * 演示 Compose 中 AnchoredDraggable 在多个锚点间拖拽切换。
 *
 * 核心特性：
 * 1. 多锚点吸附
 * 2. 平滑过渡动画
 *
 * 适用场景：
 * - 底部抽屉
 * - 开关式切换面板
 */
@Route(path = RouterPath.Compose.AnchoredDraggable)
class AnchoredDraggableActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnchoredDraggableExample()
        }
    }

    @Composable
    fun AnchoredDraggableExample() {
    }
}
