package com.example.william.my.module.compose.activity.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
 * Jetpack Compose — 多图表全景联动看板 (Chart Linkage Dashboard)
 *
 * 核心特性：
 * 1. 顶部时间轴折线图作为主控驱动器 (Master Controller)
 * 2. 状态提升 (State Hoisting) 与跨组件单向数据流分发
 * 3. 中部部门柱状图与底部获客渠道饼图毫秒级响应重组
 */
@Route(path = RouterPath.Compose.ChartLinkage)
class ComposeChartLinkageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposeChartLinkageScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposeChartLinkageScreen(modifier: Modifier = Modifier) {
    val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    val revenueTrend = listOf(45f, 52f, 48f, 65f, 78f, 85f, 80f, 92f, 88f, 105f, 98f, 120f)

    var activeMonthIndex by remember { mutableIntStateOf(5) }
    val factor = 1.0f + (activeMonthIndex * 0.08f)

    val departments = listOf("研发中心", "市场销售", "运营支持", "行政人事")
    val deptCosts = listOf(18f * factor, 12f * factor, 8f * factor, 5f * factor)

    val channels = listOf("自然搜索", "社交媒体", "效果广告", "口碑转介")
    val channelShares = listOf(35f + activeMonthIndex % 5, 25f - activeMonthIndex % 3, 20f + activeMonthIndex % 4, 20f - activeMonthIndex % 2)
    val channelColors = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEC4899))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 顶部时间轴折线图 (Master)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("主控时间轴：2026 月度营收趋势 (Compose)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("点击任意月份，下方部门成本与渠道占比将实时重组联动", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LinkageMasterLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    months = months,
                    data = revenueTrend,
                    selectedIndex = activeMonthIndex,
                    onMonthSelected = { activeMonthIndex = it }
                )
            }
        }

        // 2. 中部部门成本柱状图 (Slave 1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${months[activeMonthIndex]} 各部门成本支出 (万元)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LinkageBarChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    labels = departments,
                    values = deptCosts
                )
            }
        }

        // 3. 底部渠道获客饼图 (Slave 2)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${months[activeMonthIndex]} 获客渠道构成占比", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LinkagePieChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    labels = channels,
                    values = channelShares,
                    colors = channelColors,
                    centerLabel = months[activeMonthIndex]
                )
            }
        }
    }
}

@Composable
fun LinkageMasterLineChart(
    modifier: Modifier = Modifier,
    months: List<String>,
    data: List<Float>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit
) {
    val lineColor = Color(0xFF3B82F6)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val stepX = size.width / (months.size - 1)
                val idx = ((offset.x + stepX / 2f) / stepX).toInt().coerceIn(0, months.size - 1)
                onMonthSelected(idx)
            }
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 30f
        val chartHeight = height - paddingBottom
        val maxY = 140f
        val stepX = width / (months.size - 1)

        val path = Path()
        val fillPath = Path()
        val points = data.mapIndexed { idx, v ->
            Offset(idx * stepX, chartHeight * (1f - (v / maxY).coerceIn(0f, 1f)))
        }

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            fillPath.addPath(path)
            fillPath.lineTo(width, chartHeight)
            fillPath.lineTo(0f, chartHeight)
            fillPath.close()

            drawPath(path = fillPath, brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.02f))))
            drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        }

        // 高亮选中点
        val selX = selectedIndex * stepX
        val selY = points[selectedIndex].y
        drawLine(
            color = lineColor.copy(alpha = 0.6f),
            start = Offset(selX, 0f),
            end = Offset(selX, chartHeight),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(selX, selY))
        drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = Offset(selX, selY))

        // X 轴文字
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        months.forEachIndexed { idx, label ->
            if (idx % 2 == 0 || idx == selectedIndex) {
                drawContext.canvas.nativeCanvas.drawText(label, idx * stepX, height - 6f, paint)
            }
        }
    }
}

@Composable
fun LinkageBarChart(modifier: Modifier = Modifier, labels: List<String>, values: List<Float>) {
    val barColor = Color(0xFF10B981)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingBottom = 30f
        val chartHeight = height - paddingBottom
        val maxVal = 30f
        val groupWidth = width / labels.size
        val barWidth = 32.dp.toPx()

        labels.forEachIndexed { idx, label ->
            val cx = idx * groupWidth + groupWidth / 2f
            val h = (values[idx] / maxVal) * chartHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(cx - barWidth / 2f, chartHeight - h),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(label, cx, height - 6f, paint)
            drawContext.canvas.nativeCanvas.drawText("${String.format("%.1f", values[idx])}万", cx, chartHeight - h - 8f, paint)
        }
    }
}

@Composable
fun LinkagePieChart(modifier: Modifier = Modifier, labels: List<String>, values: List<Float>, colors: List<Color>, centerLabel: String) {
    val total = values.sum()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) / 2.3f
            val strokeWidth = 28.dp.toPx()

            var startAngle = -90f
            values.forEachIndexed { idx, v ->
                val sweep = (v / total) * 360f
                drawArc(
                    color = colors[idx],
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("渠道构成", fontSize = 11.sp, color = Color.Gray)
        }
    }
}
