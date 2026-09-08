package com.example.william.my.module.compose.activity.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Insets — 现代 Edge-to-Edge 边到边与系统栏避让
 *
 * 全面演示现代 Android（尤其是 Android 15 强制 Edge-to-Edge）下的 Compose 视口安全区处理：
 * 1. [WindowInsets.Companion.statusBars] 与 [WindowInsets.Companion.navigationBars]：状态栏与手势导航栏尺寸检测；
 * 2. [Modifier.statusBarsPadding] 与 [Modifier.navigationBarsPadding]：安全区留白修饰符；
 * 3. [WindowInsets.Companion.ime] 与 [Modifier.imePadding]：软键盘弹起高度监听与底部控件平滑上推避让；
 * 4. [Modifier.imeNestedScroll]：列表滑动连带关闭/驱动软键盘体验。
 */
@Route(path = RouterPath.Compose.Insets)
class InsetsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开启边到边模式：视图将绘制在状态栏和导航栏底部
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // 不使用默认 Scaffold 的 innerPadding 强行包裹，以便直观展示各修饰符的局部精确应用
                InsetsScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InsetsScreen() {
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 获取系统栏与 IME 的实时尺寸
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isImeVisible = WindowInsets.isImeVisible

    var inputText by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            "欢迎进入 Compose WindowInsets 实战演示！",
            "Android 15+ 强制启用 Edge-to-Edge 边到边沉浸式。",
            "顶部内容已应用 Modifier.statusBarsPadding() 避免遮挡状态栏。",
            "底部输入框已添加 Modifier.imePadding() 实现软键盘无感避让。",
            "尝试点击下方输入框调起键盘，观察各项安全区指标的实时响应！",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 顶端安全区避让
            .statusBarsPadding()
            // 底部通过 imePadding 随软键盘高度平滑弹起，键盘收起时恢复导航栏高度避让
            .imePadding()
            .navigationBarsPadding(),
    ) {
        // 顶部指标仪表盘
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "WindowInsets 实时指标看板",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "状态栏高度: ${statusBarTop.value.toInt()} dp (${with(density) { statusBarTop.roundToPx() }} px)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "导航栏高度: ${navBarBottom.value.toInt()} dp (${with(density) { navBarBottom.roundToPx() }} px)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "键盘状态: ${if (isImeVisible) "已弹出 (Visible)" else "已收起 (Hidden)"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isImeVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "IME 高度: ${imeBottom.value.toInt()} dp",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // 中间可滚动的消息列表（支持嵌套滑动连带软键盘交互 imeNestedScroll）
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imeNestedScroll()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState(),
        ) {
            items(chatMessages) { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // 底部固定的输入发送栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入内容测试键盘避让...") },
                    singleLine = true,
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    },
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            chatMessages.add(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "发送")
                }
            }
        }
    }
}
