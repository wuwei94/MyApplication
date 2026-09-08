package com.example.william.my.module.compose.activity.bottomsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.launch

/**
 * BottomSheet — Material 3 模态底栏交互示例页
 *
 * 聚合展示 Compose 中底部抽屉的标准实现：
 * 1. **ModalBottomSheet**：原生手势支持半屏吸附、全屏拖拽展开与向下甩动关闭；
 * 2. **SheetState 状态控制**：通过协程调度 `sheetState.show()` 与 `sheetState.hide()`；
 * 3. **典型工程场景**：快捷分享面板、商品规格选择器、底部操作菜单等。
 *
 * https://developer.android.google.cn/develop/ui/compose/components/bottom-sheets
 */
@Route(path = RouterPath.Compose.BottomSheet)
class BottomSheetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BottomSheetContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomSheetContent(modifier: Modifier = Modifier) {
        var showModalSheet by remember { mutableStateOf(false) }
        var skipPartiallyExpanded by remember { mutableStateOf(false) }
        var selectedActionText by remember { mutableStateOf("暂无操作") }

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Material 3 模态底栏 (ModalBottomSheet)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "支持手势上下滑动半展开/全展开与边缘遮罩防误触",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "最近底栏触发动作: $selectedActionText",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            skipPartiallyExpanded = false
                            showModalSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("打开标准两段式底栏（支持半屏展开）")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            skipPartiallyExpanded = true
                            showModalSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("打开直接全展开底栏（skipPartiallyExpanded）")
                    }
                }
            }
        }

        // 模态弹出的底栏容器
        if (showModalSheet) {
            ModalBottomSheet(
                onDismissRequest = { showModalSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "分享面板与操作",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showModalSheet = false
                                }
                            },
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ActionRowItem(icon = Icons.Default.Share, title = "分享给微信好友") {
                        selectedActionText = "已选择：分享给微信好友"
                        showModalSheet = false
                    }
                    ActionRowItem(icon = Icons.Default.ContentCopy, title = "复制页面链接") {
                        selectedActionText = "已选择：复制页面链接"
                        showModalSheet = false
                    }
                    ActionRowItem(icon = Icons.Default.Download, title = "保存分享长图至相册") {
                        selectedActionText = "已选择：保存长图"
                        showModalSheet = false
                    }
                    ActionRowItem(icon = Icons.Default.Favorite, title = "添加到收藏夹") {
                        selectedActionText = "已选择：添加到收藏夹"
                        showModalSheet = false
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    @Composable
    private fun ActionRowItem(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
