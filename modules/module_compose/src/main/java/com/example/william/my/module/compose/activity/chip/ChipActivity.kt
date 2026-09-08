package com.example.william.my.module.compose.activity.chip

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Chip — Material 3 标签芯片实战
 *
 * 全景展示 Compose Material 3 官方四大标准芯片（Chips）交互形态与语义应用场景：
 * 1. 过滤芯片 [FilterChip]：多选筛选项（支持勾选动画与带阴影的 [ElevatedFilterChip]）；
 * 2. 输入芯片 [InputChip]：实体信息卡片（如收件人标签、带关闭删除图标与用户头像）；
 * 3. 建议芯片 [SuggestionChip]：快速联想推荐与搜索引导（如“Compose M3 实战”）；
 * 4. 辅助芯片 [AssistChip]：上下文辅助操作触发项（如“添加到日历”、“一键分享”及 [ElevatedAssistChip]）。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/components/chip
 */
@Route(path = RouterPath.Compose.Chip)
class ChipActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                ChipGalleryScreen(
                    onToast = { message ->
                        Toast.makeText(this@ChipActivity, message, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ChipGalleryScreen(onToast: (String) -> Unit) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. FilterChip 多选过滤
            FilterChipSection()

            // 2. InputChip 输入实体与删除
            InputChipSection(onToast)

            // 3. SuggestionChip 快速建议
            SuggestionChipSection(onToast)

            // 4. AssistChip 辅助操作
            AssistChipSection(onToast)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun FilterChipSection() {
        val allFilters = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Room", "Hilt", "KMP")
        val selectedFilters = remember { mutableStateListOf("Kotlin", "Jetpack Compose") }

        SectionCard(title = "1. FilterChip 多选过滤标签") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "已选中: ${if (selectedFilters.isEmpty()) "无" else selectedFilters.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    allFilters.forEach { filter ->
                        val isSelected = filter in selectedFilters
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedFilters.remove(filter) else selectedFilters.add(filter)
                            },
                            label = { Text(filter) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "已勾选",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }

                    // 带阴影的 ElevatedFilterChip 变体
                    var elevatedSelected by remember { mutableStateOf(false) }
                    ElevatedFilterChip(
                        selected = elevatedSelected,
                        onClick = { elevatedSelected = !elevatedSelected },
                        label = { Text("Elevated 样式") },
                        leadingIcon = if (elevatedSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun InputChipSection(onToast: (String) -> Unit) {
        val recipients = remember {
            mutableStateListOf("Alice (架构师)", "Bob (客户端开发)", "Charlie (设计师)")
        }

        SectionCard(title = "2. InputChip 输入实体（支持删除）") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "邮件收件人列表（点击右侧 × 可移除对应收件人）：",
                    style = MaterialTheme.typography.bodySmall,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    recipients.forEach { person ->
                        InputChip(
                            selected = false,
                            onClick = { onToast("点击收件人: $person") },
                            label = { Text(person) },
                            avatar = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "删除",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            recipients.remove(person)
                                            onToast("已移除 $person")
                                        },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun SuggestionChipSection(onToast: (String) -> Unit) {
        val suggestions = listOf("Compose M3 深度实战", "Offline-First 离线架构", "StateFlow 响应式驱动", "Nav3 架构")

        SectionCard(title = "3. SuggestionChip 智能推荐与联想词") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onToast("搜索: $suggestion") },
                        label = { Text(suggestion) },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun AssistChipSection(onToast: (String) -> Unit) {
        SectionCard(title = "4. AssistChip 辅助操作指令") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { onToast("已添加到系统日历日程") },
                    label = { Text("添加到日历") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )

                ElevatedAssistChip(
                    onClick = { onToast("正在分享当前内容") },
                    label = { Text("一键分享 (Elevated)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
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
