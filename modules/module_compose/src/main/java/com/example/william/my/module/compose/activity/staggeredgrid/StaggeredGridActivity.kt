package com.example.william.my.module.compose.activity.staggeredgrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.launch

/**
 * StaggeredGrid — 交错瀑布流与网格列表示例页
 *
 * 聚合展示 Compose 中官方标准瀑布流容器的核心用法：
 * 1. **LazyVerticalStaggeredGrid**：针对高宽比各异的图文卡片进行智能紧凑流式排版；
 * 2. **列策略动态切换**：支持 2 列、3 列与自适应列宽 (StaggeredGridCells.Adaptive) 实时平滑切换；
 * 3. **状态感知与回到顶部**：利用 `rememberLazyStaggeredGridState()` 与 `derivedStateOf` 感知滚动偏移量，控制悬浮返回顶部按钮的显示与平滑滚动。
 *
 * https://developer.android.google.cn/develop/ui/compose/lists#staggered-grid
 */
@Route(path = RouterPath.Compose.StaggeredGrid)
class StaggeredGridActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                StaggeredGridScreen()
            }
        }
    }

    private data class GridCardItem(
        val id: Int,
        val title: String,
        val heightDp: Dp,
        val category: String,
        val color: Color,
        val likes: Int,
    )

    private val sampleItems = listOf(
        GridCardItem(1, "西藏林芝春季桃花摄影精选", 180.dp, "风光摄影", Color(0xFFE91E63), 1280),
        GridCardItem(2, "极简北欧风家居软装设计指南", 120.dp, "设计灵感", Color(0xFF3F51B5), 892),
        GridCardItem(3, "手冲咖啡入门：水温与研磨度的微妙黄金平衡点", 220.dp, "生活方式", Color(0xFF795548), 2410),
        GridCardItem(4, "Kotlin 2.0 现代语言特性全解", 140.dp, "移动开发", Color(0xFF009688), 3560),
        GridCardItem(5, "探索川西秘境格聂神山自驾穿越记录", 200.dp, "自驾旅行", Color(0xFFFF9800), 1734),
        GridCardItem(6, "黑胶唱片经典爵士精选集", 110.dp, "音乐鉴赏", Color(0xFF673AB7), 642),
        GridCardItem(7, "日式抹茶千层蛋糕烘焙全流程详解", 170.dp, "美食烘焙", Color(0xFF4CAF50), 3120),
        GridCardItem(8, "Jetpack Compose 自适应屏幕设计模式", 150.dp, "前端架构", Color(0xFF2196F3), 4200),
        GridCardItem(9, "室内多肉植物四季养护与防虫实战", 130.dp, "绿植花卉", Color(0xFF8BC34A), 958),
        GridCardItem(10, "复古胶片颗粒感调色 Lightroom 预设分享", 190.dp, "后期调色", Color(0xFF607D8B), 1870),
    )

    @Composable
    private fun StaggeredGridScreen() {
        var columnCount by remember { mutableIntStateOf(2) }
        val gridState = rememberLazyStaggeredGridState()
        val coroutineScope = rememberCoroutineScope()

        val showScrollToTop by remember {
            derivedStateOf { gridState.firstVisibleItemIndex > 2 }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "回到顶部",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                // 顶部控制切换栏
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "交错瀑布流 (LazyVerticalStaggeredGrid)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = columnCount == 2,
                                onClick = { columnCount = 2 },
                                label = { Text("双列排布") },
                            )
                            FilterChip(
                                selected = columnCount == 3,
                                onClick = { columnCount = 3 },
                                label = { Text("三列紧凑") },
                            )
                            FilterChip(
                                selected = columnCount == 0,
                                onClick = { columnCount = 0 },
                                label = { Text("自适应宽 (140dp)") },
                            )
                        }
                    }
                }

                // 瀑布流容器
                val cells = when (columnCount) {
                    2 -> StaggeredGridCells.Fixed(2)
                    3 -> StaggeredGridCells.Fixed(3)
                    else -> StaggeredGridCells.Adaptive(minSize = 140.dp)
                }

                LazyVerticalStaggeredGrid(
                    columns = cells,
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(sampleItems) { item ->
                        WaterfallCard(item = item)
                    }
                }
            }
        }
    }

    @Composable
    private fun WaterfallCard(item: GridCardItem) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column {
                // 模拟不同高度色彩封面图
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(item.heightDp)
                        .background(item.color.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Text(
                        text = item.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                // 标题与点赞信息
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "❤️ ${item.likes} 赞",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}
