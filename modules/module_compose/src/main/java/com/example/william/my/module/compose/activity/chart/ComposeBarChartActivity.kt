package com.example.william.my.module.compose.activity.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.abs

/**
 * Jetpack Compose — 柱状图 (BarChart)
 *
 * 核心特性：
 * 1. 季度目标 vs 实际销售额分组柱状图
 * 2. `drawRoundRect` 顶部圆角柱体绘制
 * 3. 触摸柱体即时高亮响应
 * 4. 实时联动计算差额与达成率分析
 */
@Route(path = RouterPath.Compose.BarChart)
class ComposeBarChartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposeBarChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposeBarChartScreen(modifier: Modifier = Modifier) {
    val quarters = listOf("Q1 第一季度", "Q2 第二季度", "Q3 第三季度", "Q4 第四季度")
    val targetSales = listOf(120f, 150f, 180f, 220f)
    val actualSales = listOf(135.5f, 142f, 210.8f, 245f)

    var selectedQuarterIndex by remember { mutableIntStateOf(2) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 图表卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("季度目标 vs 实际销售额 (Compose Canvas)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("分组圆角柱状图自绘，点击柱体触发高亮与分析联动", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                ComposeBarChartCore(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    selectedIndex = selectedQuarterIndex,
                    onQuarterSelected = { selectedQuarterIndex = it },
                )
            }
        }

        // 联动指标卡片
        val target = targetSales[selectedQuarterIndex]
        val actual = actualSales[selectedQuarterIndex]
        val diff = actual - target
        val rate = (actual / target) * 100

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5).copy(alpha = 0.08f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${quarters[selectedQuarterIndex]} 业绩达成分析", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = if (rate >= 100) "超额完成" else "未达预期",
                        color = if (rate >= 100) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("目标额", fontSize = 11.sp, color = Color.Gray)
                        Text("$target 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                    }
                    Column {
                        Text("实际额", fontSize = 11.sp, color = Color.Gray)
                        Text("$actual 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                    }
                    Column {
                        Text("差额", fontSize = 11.sp, color = Color.Gray)
                        Text("${if (diff > 0) "+" else ""}${String.format("%.1f", diff)} 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("达成率", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%.1f", rate)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "分析：Q${selectedQuarterIndex + 1} 实际营收 ${if (diff >= 0) "超过" else "低于"} 目标 ${abs(diff)} 万元",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
fun ComposeBarChartCore(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onQuarterSelected: (Int) -> Unit,
) {
    val targetColor = Color(0xFF3F51B5)
    val actualColor = Color(0xFF009688)
    val targetSales = listOf(120f, 150f, 180f, 220f)
    val actualSales = listOf(135.5f, 142f, 210.8f, 245f)
    val quarters = listOf("Q1", "Q2", "Q3", "Q4")

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val groupWidth = size.width / quarters.size
                val idx = (offset.x / groupWidth).toInt().coerceIn(0, quarters.size - 1)
                onQuarterSelected(idx)
            }
        },
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val maxY = 300f

        val groupWidth = width / quarters.size
        val barWidth = 24.dp.toPx()

        quarters.forEachIndexed { idx, label ->
            val groupCenterX = idx * groupWidth + groupWidth / 2f
            val isSelected = idx == selectedIndex

            val targetHeight = (targetSales[idx] / maxY) * chartHeight
            val actualHeight = (actualSales[idx] / maxY) * chartHeight

            // 目标柱
            drawRoundRect(
                color = targetColor.copy(alpha = if (isSelected) 1.0f else 0.75f),
                topLeft = Offset(groupCenterX - barWidth - 4.dp.toPx(), chartHeight - targetHeight),
                size = Size(barWidth, targetHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )

            // 实际柱
            drawRoundRect(
                color = actualColor.copy(alpha = if (isSelected) 1.0f else 0.75f),
                topLeft = Offset(groupCenterX + 4.dp.toPx(), chartHeight - actualHeight),
                size = Size(barWidth, actualHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )

            val paint = android.graphics.Paint().apply {
                color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.GRAY
                textSize = if (isSelected) 34f else 30f
                isFakeBoldText = isSelected
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(label, groupCenterX, height - 10f, paint)
        }
    }
}
