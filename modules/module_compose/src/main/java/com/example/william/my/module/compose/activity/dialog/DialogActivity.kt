package com.example.william.my.module.compose.activity.dialog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.launch

/**
 * Dialog — 对话框与 Snackbar 提示条示例页
 *
 * 聚合展示 Compose 中前台反馈体系的标准实现：
 * 1. **AlertDialog**：Material 3 标准警告确认对话框（图标、标题、文本与双动作按钮）；
 * 2. **自定义 Dialog**：基于 `Dialog` 容器与 `DialogProperties` 自定义复杂卡片交互；
 * 3. **Snackbar 浮动提示**：集成 `SnackbarHost` 与协程生命周期，支持带操作行动点（Action）的自动消隐提示。
 *
 * https://developer.android.google.cn/develop/ui/compose/components/dialog
 * https://developer.android.google.cn/develop/ui/compose/components/snackbar
 */
@Route(path = RouterPath.Compose.Dialog)
class DialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                DialogContentScreen()
            }
        }
    }

    @Composable
    private fun DialogContentScreen() {
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        var showAlertDialog by remember { mutableStateOf(false) }
        var showCustomDialog by remember { mutableStateOf(false) }
        var dialogFeedback by remember { mutableStateOf("等待操作") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. 标准 AlertDialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Material 3 标准确认弹窗 (AlertDialog)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "支持系统主题适配、图标、确认与取消双动作回调",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showAlertDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("弹出删除确认对话框")
                        }
                    }
                }

                // 2. 自定义布局 Dialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. 自定义布局对话框 (Custom Dialog)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "通过 DialogProperties 控制点击遮罩与物理返回键退出行为",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showCustomDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("弹出自定义会员权益对话框")
                        }
                    }
                }

                // 3. Snackbar 提示条
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. 浮动提示条 (Snackbar)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "通过协程驱动挂起与弹出，支持附带 Action 并捕获用户撤销动作",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "文章已成功加入稍后阅读",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Short,
                                    )
                                    dialogFeedback = when (result) {
                                        SnackbarResult.ActionPerformed -> "用户点击了【撤销】操作"
                                        SnackbarResult.Dismissed -> "Snackbar 自然超时消隐"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("弹出带【撤销】动作的 Snackbar")
                        }
                    }
                }

                // 交互结果反馈卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Text(
                        text = "当前交互反馈：$dialogFeedback",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // 1. 标准 AlertDialog
        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAlertDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("确认要清空历史缓存？") },
                text = { Text("清空后本地所有离线浏览记录将被永久移除，该操作不可撤回。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAlertDialog = false
                            dialogFeedback = "已确认清空本地缓存"
                        },
                    ) {
                        Text("立即清空", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAlertDialog = false }) {
                        Text("取消")
                    }
                },
            )
        }

        // 2. 自定义 Dialog
        if (showCustomDialog) {
            Dialog(
                onDismissRequest = { showCustomDialog = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "尊享会员特权已生效",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "您已成功解锁高清无损音质下载与专属无广告纯净模式。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                showCustomDialog = false
                                dialogFeedback = "用户已查阅自定义会员特权"
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("立即体验")
                        }
                    }
                }
            }
        }
    }
}
