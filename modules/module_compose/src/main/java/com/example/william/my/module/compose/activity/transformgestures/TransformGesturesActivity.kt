package com.example.william.my.module.compose.activity.transformgestures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

/**
 * TransformGestures — 多指复合手势实战
 *
 * 演示 Compose 官方底层手势监测 API [detectTransformGestures] 的全功能集成：
 * 1. 复合手势捕获：在单次触控监听中同步处理双指缩放（Pinch-to-zoom）、双指旋转（Rotate）与多指平移（Pan）；
 * 2. 硬件加速变换：使用 [Modifier.graphicsLayer] 独立渲染图层，避免引起频繁的全组件重组；
 * 3. 约束限制与归一化：为缩放系数（0.5f ~ 5.0f）施加安全边界约束；
 * 4. 实时参数仪表盘：显示缩放比率、旋转弧度与二维平移坐标；支持一键复位。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/touch-input/pointer-input/understand-gestures#multitouch-gestures
 */
@Route(path = RouterPath.Compose.TransformGestures)
class TransformGesturesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                TransformGesturesScreen()
            }
        }
    }

    @Composable
    private fun TransformGesturesScreen() {
        var scale by remember { mutableFloatStateOf(1f) }
        var rotation by remember { mutableFloatStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 操作说明与实时看板
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "detectTransformGestures 复合多指手势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "在下方灰色手势区内：使用双指捏合缩放、双指转动旋转、单指/双指平移拖拽。",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                    )

                    // 实时参数
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "缩放: ${(scale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "旋转: ${rotation.roundToInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "位移: (${offset.x.roundToInt()}, ${offset.y.roundToInt()})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // 2. 变换手势交互视口
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotationDelta ->
                            // 缩放边界限制：0.5x ~ 5.0x
                            scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                            rotation += rotationDelta
                            offset += pan
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                // 受手势驱动变换的图形卡片（使用 graphicsLayer 实现无重组高效矩阵变换）
                Card(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y,
                        ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Jetpack Compose",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "graphicsLayer 硬件加速",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }

            // 3. 复位按钮
            Button(
                onClick = {
                    scale = 1f
                    rotation = 0f
                    offset = Offset.Zero
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("一键复位所有手势变换参数")
            }
        }
    }
}
