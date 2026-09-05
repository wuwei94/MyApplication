package com.example.william.my.module.compose.activity.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Jetpack Compose — 折线图 (LineChart)
 *
 * 核心特性：
 * 1. `Canvas` 三次贝塞尔曲线 (`cubicTo`) 平滑插值绘制
 * 2. `Brush.verticalGradient` 渐变面积填充
 * 3. `pointerInput` 手势拖拽/点击动态吸附十字辅助虚线
 * 4. 响应式联动底部收支/利润/利润率指标卡片
 */
@Route(path = RouterPath.Compose.LineChart)
class ComposeLineChartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposeLineChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposeLineChartScreen(modifier: Modifier = Modifier) {
    val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    val incomeData = listOf(28.5f, 35.2f, 31.0f, 42.8f, 48.6f, 55.0f, 51.2f, 63.4f, 59.8f, 72.0f, 68.5f, 84.2f)
    val expenseData = listOf(18.0f, 22.4f, 26.8f, 28.0f, 34.5f, 38.2f, 35.0f, 41.5f, 39.0f, 46.2f, 44.0f, 52.6f)

    var selectedMonthIndex by remember { mutableIntStateOf(5) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 图表容器卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2026 年度收支趋势图 (Compose Canvas)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("左右滑动或点击图表可吸附十字辅助线并弹出 Tooltip", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                ComposeLineChartCore(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    income = incomeData,
                    expense = expenseData,
                    labels = months,
                    selectedIndex = selectedMonthIndex,
                    onPointSelected = { selectedMonthIndex = it },
                )
            }
        }

        // 联动指标卡片
        val inc = incomeData[selectedMonthIndex]
        val exp = expenseData[selectedMonthIndex]
        val profit = inc - exp
        val margin = (profit / inc) * 100

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00897B).copy(alpha = 0.08f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("选中月份数据联动 (${months[selectedMonthIndex]})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = if (profit >= 0) "盈利良好" else "支出预警",
                        color = if (profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("月度收入", fontSize = 11.sp, color = Color.Gray)
                        Text("$inc 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00897B))
                    }
                    Column {
                        Text("月度支出", fontSize = 11.sp, color = Color.Gray)
                        Text("$exp 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE64A19))
                    }
                    Column {
                        Text("净利润", fontSize = 11.sp, color = Color.Gray)
                        Text("${if (profit > 0) "+" else ""}${String.format("%.1f", profit)} 万", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("利润率", fontSize = 11.sp, color = Color.Gray)
                        Text("${String.format("%.1f", margin)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E24AA))
                    }
                }
            }
        }
    }
}

@Composable
fun ComposeLineChartCore(
    modifier: Modifier = Modifier,
    income: List<Float>,
    expense: List<Float>,
    labels: List<String>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
) {
    val primaryColor = Color(0xFF00897B)
    val expenseColor = Color(0xFFE64A19)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val xRatio = (change.position.x / size.width).coerceIn(0f, 1f)
                    val idx = (xRatio * (labels.size - 1)).toInt().coerceIn(0, labels.size - 1)
                    onPointSelected(idx)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val xRatio = (offset.x / size.width).coerceIn(0f, 1f)
                    val idx = (xRatio * (labels.size - 1)).toInt().coerceIn(0, labels.size - 1)
                    onPointSelected(idx)
                }
            },
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val maxY = 100f
        val stepX = width / (labels.size - 1)

        // 绘制水平虚线网格
        for (i in 0..4) {
            val y = chartHeight * (1f - i / 4f)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            )
        }

        // 收入贝塞尔曲线与渐变
        val incomePath = Path()
        val incomeFillPath = Path()
        val incomePoints = income.mapIndexed { idx, v ->
            Offset(idx * stepX, chartHeight * (1f - (v / maxY).coerceIn(0f, 1f)))
        }

        if (incomePoints.isNotEmpty()) {
            incomePath.moveTo(incomePoints.first().x, incomePoints.first().y)
            for (i in 0 until incomePoints.size - 1) {
                val p0 = incomePoints[i]
                val p1 = incomePoints[i + 1]
                val cx = (p0.x + p1.x) / 2f
                incomePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            incomeFillPath.addPath(incomePath)
            incomeFillPath.lineTo(width, chartHeight)
            incomeFillPath.lineTo(0f, chartHeight)
            incomeFillPath.close()

            drawPath(
                path = incomeFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f)),
                    startY = 0f,
                    endY = chartHeight,
                ),
            )
            drawPath(path = incomePath, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }

        // 支出折线
        val expensePath = Path()
        val expensePoints = expense.mapIndexed { idx, v ->
            Offset(idx * stepX, chartHeight * (1f - (v / maxY).coerceIn(0f, 1f)))
        }
        if (expensePoints.isNotEmpty()) {
            expensePath.moveTo(expensePoints.first().x, expensePoints.first().y)
            for (i in 0 until expensePoints.size - 1) {
                val p0 = expensePoints[i]
                val p1 = expensePoints[i + 1]
                val cx = (p0.x + p1.x) / 2f
                expensePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            drawPath(path = expensePath, color = expenseColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        }

        // 绘制高亮指示辅助线
        if (selectedIndex in labels.indices) {
            val selX = selectedIndex * stepX
            val selY = incomePoints[selectedIndex].y

            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = Offset(selX, 0f),
                end = Offset(selX, chartHeight),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
            )

            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(selX, selY))
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(selX, selY))
        }

        // X 轴文字
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        labels.forEachIndexed { idx, label ->
            if (idx % 2 == 0 || idx == selectedIndex) {
                drawContext.canvas.nativeCanvas.drawText(label, idx * stepX, height - 10f, paint)
            }
        }
    }
}
