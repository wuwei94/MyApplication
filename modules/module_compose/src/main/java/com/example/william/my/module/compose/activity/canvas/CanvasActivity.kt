package com.example.william.my.module.compose.activity.canvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Canvas — 自定义基础图形与路径绘制
 *
 * 全面展示 Compose [Canvas] 组件的底层基础绘图 API（DrawScope 原语）：
 * 1. 线段绘制：[drawLine] 线宽、端点形状（[StrokeCap.Round]）与颜色；
 * 2. 圆形与圆环：[drawCircle] 实心填充与 [Stroke] 空心圆环；
 * 3. 矩形与圆角矩形：[drawRect]、[drawRoundRect] 结合 [Brush.linearGradient] 渐变着色；
 * 4. 弧线与扇区：[drawArc] 角度跨度与 [useCenter] 闭合形态；
 * 5. 几何路径：[drawPath] 二次贝塞尔曲线与折线几何体；
 * 6. 点集绘制：[drawPoints] 离散点阵与连线模式。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/graphics/draw/overview
 */
@Route(path = RouterPath.Compose.Canvas)
class CanvasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                CanvasGalleryScreen()
            }
        }
    }

    @Composable
    private fun CanvasGalleryScreen() {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 线段与点集
            SectionCard(title = "1. 线段 (drawLine) 与 点集 (drawPoints)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // 绘制带有圆角端点的斜线
                            drawLine(
                                color = Color(0xFF2196F3),
                                start = Offset(10f, 10f),
                                end = Offset(size.width - 10f, size.height - 10f),
                                strokeWidth = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                            // 绘制反向对照线
                            drawLine(
                                color = Color(0xFFFF9800),
                                start = Offset(size.width - 10f, 10f),
                                end = Offset(10f, size.height - 10f),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                        Text("drawLine (交叉线)", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val points = listOf(
                                Offset(20f, 20f),
                                Offset(50f, 20f),
                                Offset(80f, 20f),
                                Offset(20f, 50f),
                                Offset(50f, 50f),
                                Offset(80f, 50f),
                                Offset(20f, 80f),
                                Offset(50f, 80f),
                                Offset(80f, 80f),
                            )
                            drawPoints(
                                points = points,
                                pointMode = PointMode.Points,
                                color = Color(0xFF9C27B0),
                                strokeWidth = 10.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                        Text("drawPoints (点阵)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // 2. 圆形与圆角矩形
            SectionCard(title = "2. 圆形 (drawCircle) 与 圆角矩形 (drawRoundRect)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // 实心圆与同心圆环
                            drawCircle(
                                color = Color(0xFF4CAF50),
                                radius = size.minDimension / 4,
                            )
                            drawCircle(
                                color = Color(0xFF81C784),
                                radius = size.minDimension / 2 - 4.dp.toPx(),
                                style = Stroke(width = 4.dp.toPx()),
                            )
                        }
                        Text("drawCircle (同心圆)", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // 渐变圆角矩形
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF5722), Color(0xFFFFC107)),
                                ),
                                topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                                size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                            )
                        }
                        Text("drawRoundRect (渐变)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // 3. 弧线与扇区
            SectionCard(title = "3. 弧线与扇区 (drawArc)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // 空心圆弧
                            drawArc(
                                color = Color(0xFF00BCD4),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }
                        Text("useCenter = false (半圆弧)", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // 填充扇区 (吃豆人形态)
                            drawArc(
                                color = Color(0xFFFFEB3B),
                                startAngle = 30f,
                                sweepAngle = 300f,
                                useCenter = true,
                            )
                        }
                        Text("useCenter = true (扇区)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // 4. 贝塞尔路径
            SectionCard(title = "4. 自定义几何路径 (drawPath 贝塞尔曲线)") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.7f)
                            quadraticTo(
                                size.width * 0.25f,
                                0f,
                                size.width * 0.5f,
                                size.height * 0.5f,
                            )
                            quadraticTo(
                                size.width * 0.75f,
                                size.height,
                                size.width,
                                size.height * 0.3f,
                            )
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF673AB7),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("quadraticTo 二次平滑贝塞尔波浪曲线", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    @Composable
    private fun SectionCard(
        title: String,
        content: @Composable () -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                content()
            }
        }
    }
}
