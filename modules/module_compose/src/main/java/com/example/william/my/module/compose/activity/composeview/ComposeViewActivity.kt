package com.example.william.my.module.compose.activity.composeview

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.R
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * ComposeView — 在传统 View 体系中使用 Compose
 *
 * 演示在传统 XML 布局（LinearLayout / TextView）中通过 [ComposeView] 嵌入现代 Compose 声明式 UI。
 *
 * 核心实践：
 * 1. 组合生命周期管理策略：[ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed]，
 *    保证 Compose 组合随宿主 LifecycleOwner 销毁自动清理，避免内存泄漏；
 * 2. 传统 View 与 Compose 双向状态交互：Compose 内部状态变更可触发传统 View 文本刷新；
 * 3. 渐进式迁移：无需一次性全盘重写，可在既有复杂页面（如 RecyclerView item 或 Fragment 某卡片）安全落地。
 *
 * 官方文档：
 * https://developer.android.google.cn/jetpack/compose/migrate/interoperability-apis/views-in-compose
 */
@Route(path = RouterPath.Compose.ComposeViewActivity)
class ComposeViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_compose_view)

        val traditionalTextView = findViewById<TextView>(R.id.compose_view_text)

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.apply {
            // 关键规范：配置 Dispose 策略以契合 Activity/View 生命周期
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyApplicationTheme {
                    ComposeContent(
                        onCounterChanged = { count ->
                            traditionalTextView.text = "传统 View 联动更新：Compose 累计点击了 $count 次"
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun ComposeContent(onCounterChanged: (Int) -> Unit) {
        var count by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "嵌入式 ComposeView 容器",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "当前处于 XML 布局中的 androidx.compose.ui.platform.ComposeView。\n" +
                            "支持拥有独立的声明式状态流，同时可通过回调与外层传统 View 通信。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Compose 内部状态计数: $count",
                style = MaterialTheme.typography.headlineSmall,
            )

            Button(
                onClick = {
                    count++
                    onCounterChanged(count)
                },
            ) {
                Text(text = "点击通知传统 View 更新")
            }
        }
    }
}
