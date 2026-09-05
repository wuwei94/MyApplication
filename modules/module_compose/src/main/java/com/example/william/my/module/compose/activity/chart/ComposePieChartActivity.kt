package com.example.william.my.module.compose.activity.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Jetpack Compose — 饼图/环形甜甜圈图 (PieChart)
 *
 * 核心特性：
 * 1. `drawArc` 环形自绘与扇区间隔
 * 2. `animateFloatAsState` 点击扇区放大外扩动画
 * 3. 中心区域动态显示总额与选中百分比
 * 4. 实时联动品类明细与预算分析
 */
@Route(path = RouterPath.Compose.PieChart)
class ComposePieChartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposePieChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposePieChartScreen(modifier: Modifier = Modifier) {
    val pieCategories = listOf("云计算研发", "市场营销", "人力薪酬", "办公行政", "流动储备")
    val pieAmounts = listOf(45.8f, 32.5f, 68.2f, 18.0f, 25.5f)
    val total = pieAmounts.sum()
    val colors = listOf(
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF8B5CF6),
    )

    var selectedPieIndex by remember { mutableIntStateOf(0) }

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
                Text("年度各项成本预算占比 (Compose Canvas)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("环形甜甜圈图自绘，点击扇区可触发外扩动画与中心数字联动", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                ComposePieChartCore(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    categories = pieCategories,
                    amounts = pieAmounts,
                    colors = colors,
                    selectedIndex = selectedPieIndex,
                    onSliceSelected = { selectedPieIndex = it },
                )
            }
        }

        // 联动指标卡片
        val currentAmount = pieAmounts[selectedPieIndex]
        val pct = (currentAmount / total) * 100

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors[selectedPieIndex].copy(alpha = 0.08f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("品类成本细分 — ${pieCategories[selectedPieIndex]}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("占比 ${String.format("%.1f", pct)}%", color = colors[selectedPieIndex], fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("品类名称", fontSize = 11.sp, color = Color.Gray)
                        Text(pieCategories[selectedPieIndex], fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors[selectedPieIndex])
                    }
                    Column {
                        Text("支出预算", fontSize = 11.sp, color = Color.Gray)
                        Text("$currentAmount 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("总预算额", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%.1f", total)} 万", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("全盘占比", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%.1f", pct)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                    }
                }
            }
        }
    }
}

@Composable
fun ComposePieChartCore(
    modifier: Modifier = Modifier,
    categories: List<String>,
    amounts: List<Float>,
    colors: List<Color>,
    selectedIndex: Int,
    onSliceSelected: (Int) -> Unit,
) {
    val total = amounts.sum()
    val animatedScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(300),
        label = "pieAnim",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        onSliceSelected((selectedIndex + 1) % categories.size)
                    }
                },
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (minOf(size.width, size.height) / 2.2f) * animatedScale
            val strokeWidth = 38.dp.toPx()

            var startAngle = -90f
            amounts.forEachIndexed { idx, amount ->
                val sweepAngle = (amount / total) * 360f
                val isSelected = idx == selectedIndex
                val currentRadius = if (isSelected) baseRadius + 8.dp.toPx() else baseRadius

                drawArc(
                    color = colors[idx],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = false,
                    topLeft = Offset(center.x - currentRadius, center.y - currentRadius),
                    size = Size(currentRadius * 2f, currentRadius * 2f),
                    style = Stroke(width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth, cap = StrokeCap.Round),
                )
                startAngle += sweepAngle
            }
        }

        // 中心信息
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val currentAmount = amounts[selectedIndex]
            val pct = (currentAmount / total) * 100
            Text("${String.format("%.1f", pct)}%", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(categories[selectedIndex], fontSize = 12.sp, color = Color.Gray)
        }
    }
}
