package com.example.william.my.module.compose.activity.performance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

/**
 * 标注了 @Immutable 的强稳定数据类：Compose 编译器认为其一旦创建不可变，支持 Smart Recomposition 跳过重组
 */
@Immutable
data class StableUser(
    val name: String,
    val role: String,
)

/**
 * 未标注 @Immutable 且包含普通集合的不稳定数据类：Compose 编译器无法判定其底层内容是否被外部篡改，默认视为 Unstable
 */
data class UnstableUser(
    val name: String,
    val tags: List<String>,
)

/**
 * Performance — 性能调优与重组跳过实战
 *
 * 深入演示 Compose 运行时性能优化关键机制：
 * 1. 编译器稳定性（Stability）：[@Immutable] 注解与参数稳定性对 Smart Recomposition 跳过重组的影响；
 * 2. 局部重组计数：通过 [SideEffect] 捕获组件真实执行次数，直观比对稳定与不稳定类型的重组差异；
 * 3. 削峰防抖 [derivedStateOf]：将高频流式状态（如滚动 offset 每像素变动）折叠为低频离散状态，阻断级联重组；
 * 4. 最佳实践指南：Compose Compiler Metrics、强跳过模式（Strong Skipping Mode）与 Baseline Profile。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/performance
 */
@Route(path = RouterPath.Compose.Performance)
class PerformanceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                PerformanceDemoScreen()
            }
        }
    }

    @Composable
    private fun PerformanceDemoScreen() {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // 核心技术点 1：derivedStateOf 避免高频重组
        // 只有在 firstVisibleItemIndex > 0 时状态才发生改变，避免每滑动几个像素就让整个界面重组
        val showBackToTop by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 }
        }

        // 用于触发父级重组的计数器（模拟父组件无关状态频繁刷新）
        var parentTriggerCount by remember { mutableIntStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (showBackToTop) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "回到顶部")
                    }
                }
            },
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. 原理科普卡片
                item {
                    SectionCard(title = "1. Compose 性能三支柱") {
                        Text(
                            text = "• 稳定性契约：不可变对象（@Immutable / @Stable）帮助编译器跳过重组。\n" +
                                "• 状态降频：利用 derivedStateOf 将高频连续状态折叠为离散布尔条件。\n" +
                                "• 布局轻量：避免嵌套度过深，延迟阶段读取状态（如 Modifier.offset { } 代替普通 offset）。",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                        )
                    }
                }

                // 2. 稳定性跳过重组实验场
                item {
                    SectionCard(title = "2. Smart Recomposition 稳定性对比实验") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "点击下方按钮仅改变父级局部状态，观察两个子卡片的【重组执行计数】：",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Button(
                                onClick = { parentTriggerCount++ },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("触发父级重组 (当前父级触发: $parentTriggerCount)")
                            }

                            // 稳定组件卡片
                            val stableUser = remember { StableUser("William Wu", "Android Lead") }
                            StableUserCard(user = stableUser)

                            // 不稳定组件卡片
                            val unstableUser = remember { UnstableUser("Anonymous", listOf("Dev", "Test")) }
                            UnstableUserCard(user = unstableUser)
                        }
                    }
                }

                // 3. derivedStateOf 状态削峰实战
                item {
                    SectionCard(title = "3. derivedStateOf 滚动监听削峰防抖") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "当前列表滑动首项索引: ${listState.firstVisibleItemIndex}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "showBackToTop derivedStateOf 判定: $showBackToTop",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (showBackToTop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "向下快速滑动本列表：虽然 firstVisibleItemScrollOffset 每像素都在变动，" +
                                    "但 derivedStateOf 确保只有首项越过顶部瞬间才重发状态，彻底消除了冗余重组。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // 4. 填充滚动项供滑动测试
                items(20) { index ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ),
                    ) {
                        Text(
                            text = "测试长列表项 #${index + 1}（上下滑动观察右下角回到顶部 FAB 与状态）",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun StableUserCard(user: StableUser) {
        var recomposeCount by remember { mutableIntStateOf(0) }
        SideEffect {
            recomposeCount++
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "稳定类 (@Immutable)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = "${user.name} - ${user.role}", style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = "重组: $recomposeCount 次 (成功跳过)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    @Composable
    private fun UnstableUserCard(user: UnstableUser) {
        var recomposeCount by remember { mutableIntStateOf(0) }
        SideEffect {
            recomposeCount++
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "不稳定类 (普通 List 参数)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = "${user.name} - 标签: ${user.tags}", style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = "重组: $recomposeCount 次 (被动重组)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
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
