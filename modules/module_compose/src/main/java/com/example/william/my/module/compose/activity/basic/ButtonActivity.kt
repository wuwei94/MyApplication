package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Button — 现代化 Material 3 按钮家族实战
 *
 * 全景展示 Compose Material 3 规范中的按钮体系、状态响应与交互设计：
 * 1. M3 五大核心按钮类型：Filled (Button)、Filled Tonal、Elevated、Outlined 与 TextButton；
 * 2. 图标按钮家族：IconButton、FilledIconButton、FilledTonalIconButton 与 OutlinedIconButton；
 * 3. 浮动操作按钮 (FAB)：标准 FloatingActionButton 与带文本的 ExtendedFloatingActionButton；
 * 4. 状态与加载反馈：禁用态（enabled = false）与异步加载态（CircularProgressIndicator 联动）；
 * 5. 形状与定制：胶囊形 (CircleShape)、方圆角等自定义 Shape。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/components/button
 */
@Route(path = RouterPath.Compose.Button)
class ButtonActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                ButtonGalleryScreen(
                    onToast = { message ->
                        Toast.makeText(this@ButtonActivity, message, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ButtonGalleryScreen(onToast: (String) -> Unit) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Material 3 五大核心按钮
            SectionCard(title = "1. Material 3 五大核心按钮类型") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { onToast("Filled Button 点击") }) {
                        Text("Filled")
                    }

                    FilledTonalButton(onClick = { onToast("Filled Tonal 点击") }) {
                        Text("Tonal")
                    }

                    ElevatedButton(onClick = { onToast("Elevated 点击") }) {
                        Text("Elevated")
                    }

                    OutlinedButton(onClick = { onToast("Outlined 点击") }) {
                        Text("Outlined")
                    }

                    TextButton(onClick = { onToast("TextButton 点击") }) {
                        Text("Text")
                    }
                }
            }

            // 2. 带图标按钮与图标按钮族
            SectionCard(title = "2. 图标按钮与混排按钮") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(onClick = { onToast("发送按钮点击") }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("带图标按钮")
                        }

                        ElevatedButton(onClick = { onToast("收藏按钮点击") }) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("收藏")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onToast("Standard IconButton") }) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                        }
                        FilledIconButton(onClick = { onToast("FilledIconButton") }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                        FilledTonalIconButton(onClick = { onToast("FilledTonalIconButton") }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                        OutlinedIconButton(onClick = { onToast("OutlinedIconButton") }) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Outlined")
                        }
                    }
                }
            }

            // 3. 悬浮操作按钮 (FAB)
            SectionCard(title = "3. 悬浮操作按钮 (Floating Action Button)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FloatingActionButton(
                        onClick = { onToast("FAB 点击") },
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add FAB")
                    }

                    ExtendedFloatingActionButton(
                        onClick = { onToast("Extended FAB 点击") },
                        icon = { Icon(Icons.Filled.Navigation, contentDescription = null) },
                        text = { Text(text = "导航路线") },
                    )
                }
            }

            // 4. 禁用态与异步加载状态
            SectionCard(title = "4. 交互状态反馈（禁用与加载态）") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {},
                            enabled = false,
                        ) {
                            Text("已禁用 (Disabled)")
                        }

                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                        ) {
                            Text("描边禁用态")
                        }
                    }

                    // 异步 Loading 按钮
                    Button(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                coroutineScope.launch {
                                    delay(1500)
                                    isLoading = false
                                    onToast("提交成功！")
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("提交中...")
                        } else {
                            Text("点击触发异步加载态 (模拟 1.5s)")
                        }
                    }
                }
            }

            // 5. 自定义形状与胶囊形
            SectionCard(title = "5. 自定义形状定制") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { onToast("胶囊按钮点击") },
                        shape = CircleShape,
                    ) {
                        Text("胶囊形 (Pill Shape)")
                    }

                    Button(
                        onClick = { onToast("直角微圆角点击") },
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("微圆角 (4.dp)")
                    }
                }
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
