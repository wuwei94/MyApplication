package com.example.william.my.module.compose.activity.tab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.launch

/**
 * ScrollableTabRow 与 HorizontalPager 双向联动示例页
 *
 * 聚合展示 Compose 中可滚动标签栏与水平分页容器的深度协同：
 * 1. **双向联动**：手势滑动分页（HorizontalPager）驱动 Tab 选中项跟随，点击 Tab 平滑滚动至目标页面；
 * 2. **程序化跳页**：通过 `PagerState.animateScrollToPage()` 支持上一页、下一页及快速定点跳转；
 * 3. **分页指示器**：自定义圆点指示器动态响应当前页码与总页数。
 *
 * https://developer.android.google.cn/develop/ui/compose/components/tab-row
 * https://developer.android.google.cn/jetpack/compose/layouts/pager
 */
@Route(path = RouterPath.Compose.ScrollableTab)
class ScrollableTabActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScrollableTabPagerContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun ScrollableTabPagerContent(modifier: Modifier = Modifier) {
        val tabs = listOf("推荐", "热门", "科技", "数码", "影视娱乐", "生活旅行", "知识精选", "关于我们")
        val pagerState = rememberPagerState { tabs.size }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "可滚动 Tab 与 HorizontalPager 联动",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "支持手势左右横滑与点击 Tab 平滑切换，双向保持同步",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 可横向滚动的 Tab 栏
            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. HorizontalPager 分页内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = tabs[page],
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "第 ${page + 1} / ${tabs.size} 页",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "向左或向右滑动屏幕切换页面",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 圆点指示器 (Pager Indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(tabs.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                    val width = if (isSelected) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 程序化翻页控制按钮区
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(
                    enabled = pagerState.currentPage > 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                ) {
                    Text("上一页")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val target = if (pagerState.currentPage == tabs.size - 1) 0 else tabs.size - 1
                            pagerState.animateScrollToPage(target)
                        }
                    },
                ) {
                    Text(if (pagerState.currentPage == tabs.size - 1) "回到首页" else "跳转至末尾")
                }

                OutlinedButton(
                    enabled = pagerState.currentPage < tabs.size - 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                ) {
                    Text("下一页")
                }
            }
        }
    }
}
