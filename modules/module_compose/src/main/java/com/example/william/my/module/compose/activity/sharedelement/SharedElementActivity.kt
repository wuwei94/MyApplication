package com.example.william.my.module.compose.activity.sharedelement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * SharedElement — 声明式共享元素转场动效示例页
 *
 * 聚合展示 Compose 1.7+ / Navigation 2.8+ 核心共享元素过渡技术：
 * 1. **SharedTransitionLayout**：开辟跨层级的共享过渡坐标空间；
 * 2. **Modifier.sharedElement**：图片等特定视觉元素在列表项与全屏大图间的连续无缝位移与缩放；
 * 3. **Modifier.sharedBounds**：卡片容器背景与文本标题在不同层级边界间的平滑形变扩展；
 * 4. **转场连续性**：消除了传统 Fragment/Activity 页面跳转时的割裂感，保证视觉焦点不中断。
 *
 * https://developer.android.google.cn/develop/ui/compose/animation/shared-elements
 */
@Route(path = RouterPath.Compose.SharedElement)
class SharedElementActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                SharedElementScreen()
            }
        }
    }

    private data class DestinationItem(
        val id: Int,
        val title: String,
        val location: String,
        val color: Color,
        val description: String,
    )

    private val destinations = listOf(
        DestinationItem(
            id = 1,
            title = "富士山与河口湖",
            location = "日本·山梨县",
            color = Color(0xFF2979FF),
            description = "富士五湖之一，晨曦倒影富士山绝佳观赏地。春季樱花盛放与秋季枫叶回廊相映成趣。",
        ),
        DestinationItem(
            id = 2,
            title = "圣托里尼蓝顶教堂",
            location = "希腊·爱琴海",
            color = Color(0xFF00B0FF),
            description = "爱琴海明珠，标志性纯白墙面搭配钴蓝穹顶，坐拥世界上最美丽的日落胜景。",
        ),
        DestinationItem(
            id = 3,
            title = "冰岛黑沙滩玄武岩柱",
            location = "冰岛·维克小镇",
            color = Color(0xFF37474F),
            description = "大西洋巨浪冲刷下的火山熔岩黑沙滩，排列规整的六角玄武岩石柱群恍如异星世界。",
        ),
        DestinationItem(
            id = 4,
            title = "佩特拉玫瑰古城",
            location = "约旦·马安省",
            color = Color(0xFFFF6D00),
            description = "在红色砂岩峡谷中雕凿而成的纳巴泰王国遗迹，卡兹尼神殿在晨光中散发玫瑰色光辉。",
        ),
    )

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    private fun SharedElementScreen() {
        var selectedItem by remember { mutableStateOf<DestinationItem?>(null) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SharedTransitionLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                AnimatedContent(
                    targetState = selectedItem,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
                    },
                    label = "SharedTransitionContent",
                ) { targetItem ->
                    if (targetItem == null) {
                        // 列表模式
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = "共享元素转场 (SharedTransitionLayout)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "点击下方任一卡片，体验图片与卡片容器向全屏详情的无缝平滑过渡",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(destinations) { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .sharedBounds(
                                                sharedContentState = rememberSharedContentState(key = "card_bounds_${item.id}"),
                                                animatedVisibilityScope = this@AnimatedContent,
                                            )
                                            .clickable { selectedItem = item },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        ) {
                                            // 共享元素：图标/色彩方块
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .sharedElement(
                                                        sharedContentState = rememberSharedContentState(key = "icon_${item.id}"),
                                                        animatedVisibilityScope = this@AnimatedContent,
                                                    )
                                                    .clip(CircleShape)
                                                    .background(item.color),
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = item.location,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 详情全屏卡片模式
                        BackHandler {
                            selectedItem = null
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "card_bounds_${targetItem.id}"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                )
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedItem = null }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                }
                                Text(text = "目的地详情", style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 共享元素：放大为大面积矩形横幅
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "icon_${targetItem.id}"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(targetItem.color),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = targetItem.location,
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = targetItem.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = targetItem.description,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { selectedItem = null },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("返回列表 (或按物理返回键)")
                            }
                        }
                    }
                }
            }
        }
    }
}
