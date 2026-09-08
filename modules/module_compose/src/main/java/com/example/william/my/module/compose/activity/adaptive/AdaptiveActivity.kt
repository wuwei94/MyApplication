package com.example.william.my.module.compose.activity.adaptive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Adaptive — 响应式多端自适应布局示例页
 *
 * 聚合展示 Compose 官方规范的自适应大屏与折叠屏适配架构：
 * 1. **WindowSizeClass 规范断点**：
 *    - **Compact (< 600dp)**：常见直板手机，采用单栏流式布局；
 *    - **Medium (600dp..840dp)**：折叠屏内屏/小平板，采用紧凑双栏；
 *    - **Expanded (> 840dp)**：大平板与横屏，展开完整的 List-Detail（列表-详情）双窗格规范；
 * 2. **List-Detail 模式落地**：
 *    - 手机端：点击条目覆盖进入详情，支持物理返回键返回；
 *    - 平板/折叠屏端：左右分栏同屏响应，左侧列表常驻，右侧毫秒级切换详情看板；
 * 3. **内置模拟切换**：无需实体平板或旋转屏幕，支持在页面内手动模拟 Compact、Medium 与 Expanded 视口。
 *
 * https://developer.android.google.cn/develop/ui/compose/layouts/adaptive
 */
@Route(path = RouterPath.Compose.Adaptive)
class AdaptiveActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdaptiveScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private enum class SimulationMode(val label: String, val simulatedWidth: Dp?) {
        AUTO("真实视口", null),
        COMPACT("手机 (<600dp)", 380.dp),
        MEDIUM("折叠屏 (700dp)", 700.dp),
        EXPANDED("平板 (>840dp)", 900.dp),
    }

    private data class ArticleItem(val id: Int, val title: String, val category: String, val summary: String)

    private val sampleArticles = listOf(
        ArticleItem(1, "Jetpack Compose 1.8 核心演化解读", "架构技术", "深入剖析 Compose 编译期优化、稳定性系统及多平台生态。"),
        ArticleItem(2, "Navigation 3 声明式解耦架构演进", "导航规范", "NavKey 强类型路由与纯状态提升返回栈的全面实践。"),
        ArticleItem(3, "Material 3 响应式设计指南", "UI 规范", "从直板手机、折叠屏到平板电脑的 List-Detail 经典断点。"),
        ArticleItem(4, "Kotlin 2.0 K2 编译器原理", "语言特性", "全新的前端编译器架构，大幅提高大型多模块工程编译效率。"),
        ArticleItem(5, "Offline-First 离线优先架构实战", "工程基建", "Room + MMKV 游标同步与 SSOT 单一数据源的稳定落地。"),
    )

    @Composable
    private fun AdaptiveScreen(modifier: Modifier = Modifier) {
        var simMode by remember { mutableStateOf(SimulationMode.AUTO) }
        var selectedArticleId by remember { mutableStateOf<Int?>(1) }
        var showDetailOnCompact by remember { mutableStateOf(false) }

        val realScreenWidth = LocalConfiguration.current.screenWidthDp.dp
        val activeWidth = simMode.simulatedWidth ?: realScreenWidth
        val isExpandedWindow = activeWidth >= 600.dp

        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            // 控制面板卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "自适应断点模拟器 (当前视口: ${activeWidth.value.toInt()}dp)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "断点判定: ${if (isExpandedWindow) "【双窗格模式】(>=600dp)" else "【单栏模式】(<600dp)"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpandedWindow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SimulationMode.entries.forEach { mode ->
                            FilterChip(
                                selected = simMode == mode,
                                onClick = {
                                    simMode = mode
                                    if (mode.simulatedWidth != null && mode.simulatedWidth >= 600.dp) {
                                        showDetailOnCompact = false
                                    }
                                },
                                label = { Text(mode.label) },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 响应式画布容器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                val selectedArticle = sampleArticles.find { it.id == selectedArticleId } ?: sampleArticles.first()

                if (isExpandedWindow) {
                    // 大屏/平板/折叠屏：List-Detail 双窗格布局
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 左侧列表窗格 (占比 40%)
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                        ) {
                            ArticleListView(
                                articles = sampleArticles,
                                selectedId = selectedArticleId,
                                onItemClick = { article ->
                                    selectedArticleId = article.id
                                },
                            )
                        }

                        // 竖向分割线
                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )

                        // 右侧详情窗格 (占比 60%)
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .padding(24.dp),
                        ) {
                            ArticleDetailView(article = selectedArticle, onBackClick = null)
                        }
                    }
                } else {
                    // 紧凑直板手机：单栏流式导航
                    if (showDetailOnCompact) {
                        BackHandler {
                            showDetailOnCompact = false
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        ) {
                            ArticleDetailView(
                                article = selectedArticle,
                                onBackClick = { showDetailOnCompact = false },
                            )
                        }
                    } else {
                        ArticleListView(
                            articles = sampleArticles,
                            selectedId = selectedArticleId,
                            onItemClick = { article ->
                                selectedArticleId = article.id
                                showDetailOnCompact = true
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArticleListView(
        articles: List<ArticleItem>,
        selectedId: Int?,
        onItemClick: (ArticleItem) -> Unit,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            items(articles) { article ->
                val isSelected = article.id == selectedId
                val containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onItemClick(article) },
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArticleDetailView(article: ArticleItem, onBackClick: (() -> Unit)?) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onBackClick != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回列表")
                    }
                    Text(text = "返回列表", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "分类归属：${article.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(
                    text = "自适应机制说明：在宽屏模式下，左侧列表常驻，右侧立即刷新无需转场路由；在窄屏模式下，点击条目自动下钻详情，按物理返回键自动复位。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}
