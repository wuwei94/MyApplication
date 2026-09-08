package com.example.william.my.module.compose.activity.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Menu — 现代弹出菜单与表单下拉选择
 *
 * 全面演示 Material 3 菜单体系与表单选择组件：
 * 1. [DropdownMenu]：标准悬浮上下文菜单，支持 leadingIcon 图标、trailingIcon 快捷键提示、
 *    [HorizontalDivider] 视觉分组与禁用项控制；
 * 2. [ExposedDropdownMenuBox]：Material 3 官方推荐表单下拉菜单（取代传统 Android Spinner 控件），
 *    支持只读输入框触发、展开箭头旋转动画及选项高亮状态。
 */
@Route(path = RouterPath.Compose.Menu)
class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MenuScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun MenuScreen(modifier: Modifier = Modifier) {
    var selectedActionLog by remember { mutableStateOf("暂无操作，点击菜单体验") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 状态看板
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "当前操作日志",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedActionLog,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // 1. 标准 DropdownMenu（基础弹出菜单）
        StandardDropdownMenuCard(
            onActionSelected = { selectedActionLog = "触发菜单项：$it" },
        )

        // 2. 表单下拉选择 ExposedDropdownMenuBox（替代传统 Spinner）
        ExposedDropdownMenuCard(
            onOptionSelected = { selectedActionLog = "表单已选择：$it" },
        )
    }
}

/**
 * 标准 DropdownMenu 交互卡片
 */
@Composable
private fun StandardDropdownMenuCard(
    onActionSelected: (String) -> Unit,
) {
    var showButtonMenu by remember { mutableStateOf(false) }
    var showIconMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "1. DropdownMenu（标准弹出菜单）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "通过锚点触发展开浮动层，支持图标前缀、快捷键后缀、分割线及禁用状态控制。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 按钮锚点
                Box {
                    Button(onClick = { showButtonMenu = true }) {
                        Text(text = "展开操作菜单")
                    }

                    DropdownMenu(
                        expanded = showButtonMenu,
                        onDismissRequest = { showButtonMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑内容") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            trailingIcon = {
                                Text(
                                    text = "Ctrl+E",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                showButtonMenu = false
                                onActionSelected("编辑内容")
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("分享链接") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showButtonMenu = false
                                onActionSelected("分享链接")
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("收藏项（禁用）") },
                            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                            enabled = false,
                            onClick = {
                                showButtonMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除记录") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                showButtonMenu = false
                                onActionSelected("删除记录")
                            },
                        )
                    }
                }

                // 图标按钮锚点
                Box {
                    OutlinedButton(onClick = { showIconMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "更多选项")
                    }

                    DropdownMenu(
                        expanded = showIconMenu,
                        onDismissRequest = { showIconMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("复制配置") },
                            onClick = {
                                showIconMenu = false
                                onActionSelected("复制配置")
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("重置默认") },
                            onClick = {
                                showIconMenu = false
                                onActionSelected("重置默认")
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * ExposedDropdownMenuBox 表单下拉选择框卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuCard(
    onOptionSelected: (String) -> Unit,
) {
    val options = listOf("Kotlin 协程并发", "Jetpack Compose 声明式 UI", "Room 离线优先数据库", "Koin 依赖注入")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "2. ExposedDropdownMenuBox（表单下拉选择框）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Material 3 规范表单选择组件，将输入框与下拉列表结合，具备自动展开旋转箭头与全宽约束对齐。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = selectedOptionText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("技术栈主题") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEach { selectionOption ->
                        val isSelected = selectionOption == selectedOptionText
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = selectionOption,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "已选择",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                            onClick = {
                                selectedOptionText = selectionOption
                                expanded = false
                                onOptionSelected(selectionOption)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
    }
}
