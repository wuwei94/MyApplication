package com.example.william.my.module.compose.activity.androidview

import android.graphics.Color
import android.os.Bundle
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * AndroidView — 传统 View 嵌入 Compose 互操作示例页
 *
 * 与 [com.example.william.my.module.compose.activity.ComposeViewActivity] 形成对偶闭环：
 * 1. **ComposeView**：在传统 XML/View 体系中开辟 Compose 渲染树；
 * 2. **AndroidView**：在 Compose 声明式树中托管原生 Android View（常用于地图 SDK、Player Surface、复杂三方原生图表等无法纯 Compose 化的场景）。
 *
 * 核心机制：
 * - `factory`：只在首次挂载时执行一次，用于实例化原生 View 并完成初始化配置；
 * - `update`：在关联的 Compose State 发生变化引起重组时被反复调用，执行属性更新。
 *
 * https://developer.android.google.cn/develop/ui/compose/migrate/interoperability-apis/views-in-compose
 */
@Route(path = RouterPath.Compose.AndroidView)
class AndroidViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidViewContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun AndroidViewContent(modifier: Modifier = Modifier) {
        var progress by remember { mutableFloatStateOf(45f) }
        var selectedDate by remember {
            mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Compose 驱动原生 View 更新 (State -> View)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Compose 状态驱动原生 View 更新",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "滑动 Compose Slider，在 update 回调中实时驱动原生 ProgressBar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "当前设定进度: ${progress.roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 嵌入原生 ProgressBar
                    AndroidView(
                        factory = { context ->
                            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                                max = 100
                            }
                        },
                        update = { progressBar ->
                            progressBar.progress = progress.roundToInt()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 嵌入原生 TextView
                    AndroidView(
                        factory = { context ->
                            TextView(context).apply {
                                textSize = 14f
                                setTextColor(Color.DKGRAY)
                            }
                        },
                        update = { textView ->
                            textView.text = "【原生 TextView 反映】进度已达 ${progress.roundToInt()}%"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // 2. 原生 View 事件驱动 Compose 状态变化 (View -> State)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 原生 View 事件驱动 Compose 重组",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "在 factory 中监听 CalendarView 日期选中事件，回调更新 Compose 状态",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Compose 选定日期看板: $selectedDate",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 嵌入原生日历 CalendarView
                    AndroidView(
                        factory = { context ->
                            CalendarView(context).apply {
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    val formattedMonth = (month + 1).toString().padStart(2, '0')
                                    val formattedDay = dayOfMonth.toString().padStart(2, '0')
                                    selectedDate = "$year-$formattedMonth-$formattedDay"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
