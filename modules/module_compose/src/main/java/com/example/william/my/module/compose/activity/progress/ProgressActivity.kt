package com.example.william.my.module.compose.activity.progress

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

/**
 * Progress — 现代进度指示器原语
 *
 * 全面演示 Material 3 基础加载与进度指示器组件：
 * 1. [LinearProgressIndicator]：线性进度条，展示非确定性（无限加载）与确定性（0% ~ 100% 进度跟踪）；
 * 2. [CircularProgressIndicator]：圆形环形进度条，展示非确定性转圈与确定性环形闭合；
 * 3. 样式原语定制：轨道背景色（trackColor）、圆角端点（[StrokeCap.Round]）、线条粗细（strokeWidth）。
 */
@Route(path = RouterPath.Compose.Progress)
class ProgressActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProgressScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(modifier: Modifier = Modifier) {
    var progress by remember { mutableFloatStateOf(0.4f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 进度控制卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "确定性进度控制看板: ${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..1f,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { progress = 0f },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("重置 (0%)")
                    }
                    OutlinedButton(
                        onClick = { progress = (progress + 0.25f).coerceAtMost(1f) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("增加 (+25%)")
                    }
                    Button(
                        onClick = { progress = 1f },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("完成 (100%)")
                    }
                }
            }
        }

        // 1. 线性进度条 LinearProgressIndicator
        LinearProgressIndicatorCard(progress = animatedProgress)

        // 2. 圆形进度指示器 CircularProgressIndicator
        CircularProgressIndicatorCard(progress = animatedProgress)
    }
}

/**
 * 线性进度指示器卡片
 */
@Composable
private fun LinearProgressIndicatorCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "1. LinearProgressIndicator（线性进度条）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // 非确定性（无限加载）
            Text(
                text = "非确定性进度（无极加载动画，用于未知耗时任务）:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            // 确定性（确定进度）
            Text(
                text = "确定性进度（平滑动态推进，带圆角端点）:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

/**
 * 圆形进度指示器卡片
 */
@Composable
private fun CircularProgressIndicatorCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "2. CircularProgressIndicator（圆形进度指示器）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 非确定性旋转环
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Text(
                        text = "非确定性转圈",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                // 确定性环形闭合
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 5.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = "确定性闭合 ${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                // 细线条风格
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = "轻量细线环",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
