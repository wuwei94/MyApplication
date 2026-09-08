package com.example.william.my.module.compose.activity.anchoreddraggable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 侧滑操作项状态锚点定义
 */
private enum class DragValue {
    Settled, // 正常未侧滑状态
    Revealed, // 侧滑露出右侧操作按钮状态
}

/**
 * AnchoredDraggable — 锚点吸附手势
 *
 * 演示 Compose 官方现代手势组件 [Modifier.anchoredDraggable] 实现列表项侧滑露出操作项（Swipe-to-Reveal）。
 *
 * 核心机制：
 * 1. 锚点定位：通过 [DraggableAnchors] 为不同状态设定像素位移锚点；
 * 2. 状态驱动：[AnchoredDraggableState] 控制当前展开/折叠状态；
 * 3. 阈值与动画：结合 [spring] 弹性动画与 [positionalThreshold] 判定松手吸附逻辑；
 * 4. 编程式控制：支持手势拖拽的同时，可通过协程调用 [AnchoredDraggableState.animateTo] 编程式触发展开/复位。
 *
 * 官方文档：
 * https://developer.android.google.cn/reference/kotlin/androidx/compose/foundation/gestures/package-summary#anchoredDraggable
 */
@Route(path = RouterPath.Compose.AnchoredDraggable)
class AnchoredDraggableActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AnchoredDraggableDemo()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun AnchoredDraggableDemo() {
        val density = LocalDensity.current
        val coroutineScope = rememberCoroutineScope()
        val actionSize = 80.dp
        val actionSizePx = with(density) { (actionSize * 2).toPx() }

        val state = remember {
            AnchoredDraggableState(
                initialValue = DragValue.Settled,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 125.dp.toPx() } },
                snapAnimationSpec = spring(),
                decayAnimationSpec = exponentialDecay(),
            ).apply {
                updateAnchors(
                    DraggableAnchors {
                        DragValue.Settled at 0f
                        DragValue.Revealed at -actionSizePx
                    },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AnchoredDraggable 侧滑露出操作项",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "向左滑动手势可拉出隐藏的操作按钮，松手根据滑动距离自动吸附；支持点击外部或按钮编程式复位。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // 侧滑容器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E0E0)),
            ) {
                // 底层操作项背景（侧滑后露出）
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                state.animateTo(DragValue.Settled)
                            }
                        },
                        modifier = Modifier
                            .width(actionSize)
                            .height(88.dp)
                            .background(Color(0xFFFF9800)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Archive,
                            contentDescription = "归档",
                            tint = Color.White,
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                state.animateTo(DragValue.Settled)
                            }
                        },
                        modifier = Modifier
                            .width(actionSize)
                            .height(88.dp)
                            .background(Color(0xFFF44336)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = Color.White,
                        )
                    }
                }

                // 表层卡片内容（跟随拖拽平移）
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                x = state.requireOffset().roundToInt(),
                                y = 0,
                            )
                        }
                        .anchoredDraggable(
                            state = state,
                            orientation = Orientation.Horizontal,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "邮件 / 消息项（向左滑动）",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "状态: ${if (state.currentValue == DragValue.Settled) "默认状态" else "已展开操作项"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 编程式切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            state.animateTo(DragValue.Revealed)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("编程式展开")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            state.animateTo(DragValue.Settled)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("编程式复位")
                }
            }
        }
    }
}
