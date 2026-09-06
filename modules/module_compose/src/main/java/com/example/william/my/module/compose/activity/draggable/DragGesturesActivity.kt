package com.example.william.my.module.compose.activity.draggable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.roundToInt

/**
 * DragGestures — 拖拽手势
 *
 * 演示 Compose 中 drag 手势的检测与处理，实现元素拖拽。
 *
 * 核心特性：
 * 1. detectDragGestures 检测拖拽
 * 2. 实时更新元素偏移
 *
 * 适用场景：
 * - 可拖拽组件
 * - 手势交互
 */
@Route(path = RouterPath.Compose.DragGestures)
class DragGesturesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DragGesturesExample()
        }
    }

    @Composable
    private fun DragGesturesExample() {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            Text(
                modifier = Modifier
                    .offset {
                        IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                    .background(Color.Blue)
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                text = "Drag me!",
            )
        }
    }
}
