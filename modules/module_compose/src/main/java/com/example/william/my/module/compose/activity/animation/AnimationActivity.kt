package com.example.william.my.module.compose.activity.animation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Animation — 声明式动画与状态平滑过渡示例页
 *
 * 聚合展示 Compose 中现代动画 API 的核心体系：
 * 1. **可见性动画 (AnimatedVisibility)**：元素进出场的淡入淡出 (fadeIn/fadeOut) 与垂直展开折叠；
 * 2. **容器尺寸形变 (animateContentSize)**：布局尺寸随内容动态增减时的平滑形变；
 * 3. **状态交叉淡入淡出 (Crossfade)**：不同状态或视图切页时的平滑交替过渡；
 * 4. **值动画 (animate*AsState)**：颜色与尺寸根据布尔状态平滑过渡插值；
 * 5. **无限循环动效 (rememberInfiniteTransition)**：呼吸灯与 360° 无限旋转 Loading 指示器。
 *
 * https://developer.android.google.cn/develop/ui/compose/animation
 */
@Route(path = RouterPath.Compose.Animation)
class AnimationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimationContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun AnimationContent(modifier: Modifier = Modifier) {
        var isVisible by remember { mutableStateOf(true) }
        var isExpanded by remember { mutableStateOf(false) }
        var isToggled by remember { mutableStateOf(false) }
        var crossfadeState by remember { mutableIntStateOf(0) }

        // 1. 无限旋转动画
        val infiniteTransition = rememberInfiniteTransition(label = "infinite")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "rotation",
        )
        val breathingAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "breathing",
        )

        // 2. 状态插值动画
        val animatedColor by animateColorAsState(
            targetValue = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            animationSpec = tween(durationMillis = 600),
            label = "colorAnim",
        )
        val animatedRadius by animateDpAsState(
            targetValue = if (isToggled) 28.dp else 8.dp,
            animationSpec = tween(durationMillis = 600),
            label = "radiusAnim",
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 可见性动画 AnimatedVisibility
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 可见性转场 (AnimatedVisibility)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "支持展开/折叠与透明度复合转场效果",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { isVisible = !isVisible },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isVisible) "收起隐藏内容卡片" else "展开显示内容卡片")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "✨ 动态淡入淡出与竖向展开的平滑卡片",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            // 2. 容器尺寸形变 animateContentSize
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(400)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(16.dp),
                ) {
                    Text(
                        text = "2. 容器尺寸自适应形变 (animateContentSize)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "点击卡片切换段落折叠/展开，高度自动补间平滑过渡",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isExpanded) {
                            "Compose 引入了强大的声明式动画子系统。无需再像传统 View 体系那样繁琐地创建 ValueAnimator、监听 onAnimationUpdate 并手动修改 LayoutParams。只需在 Modifier 上追加 .animateContentSize()，当文本折叠或追加行数时，容器高度便能以极具物理质感的速度缓动伸缩。"
                        } else {
                            "点击此处立即展开长文本，查看容器尺寸平滑伸缩动效...（点击切换）"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // 3. 状态属性插值动画
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. 属性插值动画 (animateColor & animateDp)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "点击按钮平滑过渡圆角弧度与主题背景色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(animatedRadius))
                            .background(animatedColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "圆角: ${animatedRadius.value.toInt()}dp · 色彩即时过渡",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { isToggled = !isToggled },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("切换形态与色彩")
                    }
                }
            }

            // 4. Crossfade 状态过渡与无限动画
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Crossfade 交叉淡入淡出与无限旋转动效",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 360° 旋转 Loading
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .rotate(rotationAngle),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("360° 旋转", style = MaterialTheme.typography.labelSmall)
                        }

                        // 呼吸灯光晕
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = breathingAlpha)),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("呼吸灯脉冲", style = MaterialTheme.typography.labelSmall)
                        }

                        // Crossfade 视图状态切换
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Crossfade(
                                targetState = crossfadeState,
                                animationSpec = tween(400),
                                label = "crossfade",
                            ) { state ->
                                val text = when (state) {
                                    0 -> "状态 A"
                                    1 -> "状态 B"
                                    else -> "状态 C"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = text, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "点击切换",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable {
                                    crossfadeState = (crossfadeState + 1) % 3
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
