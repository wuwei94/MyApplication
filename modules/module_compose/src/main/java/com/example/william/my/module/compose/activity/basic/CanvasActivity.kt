package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Canvas — 自定义绘制
 *
 * 演示 Compose 中 Canvas 的绘制能力，使用 DrawScope 绘制图形与路径。
 *
 * 核心特性：
 * 1. 绘制图形、路径与文字
 * 2. 支持旋转、缩放等变换
 *
 * 适用场景：
 * - 自定义图形与图表
 * - 手势绘制
 *
 * https://developer.android.google.cn/jetpack/compose/graphics/draw/overview
 */
@Route(path = RouterPath.Compose.Canvas)
class CanvasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanvasExample()
        }
    }

    @Composable
    fun CanvasExample() {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 绘制一条从右上角到左下角的蓝色的线
            drawLine(
                start = Offset(x = canvasWidth, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = Color.Blue,
            )

            // 以 (200, 200) 为圆心、120 为半径绘制一个圆
            drawCircle(
                color = Color.Blue,
                radius = 120f,
                center = Offset(200f, 200f),
            )
        }
    }
}
