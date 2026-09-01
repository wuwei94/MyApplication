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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Jetpack Compose — 六维能力评估雷达图 (RadarChart)
 *
 * 核心特性：
 * 1. 4 层蛛网正多边形三角函数计算自绘
 * 2. 双数据集半透明填充覆盖对比（自我评定 vs 团队基准）
 * 3. 顶点触控即时切换维度高亮
 * 4. 实时联动能力等级判定与差距分析
 */
@Route(path = RouterPath.Compose.RadarChart)
class ComposeRadarChartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposeRadarChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposeRadarChartScreen(modifier: Modifier = Modifier) {
    val radarDims = listOf("架构设计", "性能优化", "源码理解", "跨端实战", "工程运维", "团队协作")
    val radarSelf = listOf(92f, 88f, 85f, 95f, 78f, 89f)
    val radarTarget = listOf(85f, 80f, 90f, 85f, 75f, 80f)

    var selectedDimIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 图表卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("六维技术能力评估模型 (Compose Canvas)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("蛛网多边形网格自绘，点击任意维度可切换高亮与能力评定", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                ComposeRadarChartCore(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    dims = radarDims,
                    selfScores = radarSelf,
                    targetScores = radarTarget,
                    selectedIndex = selectedDimIndex,
                    onVertexSelected = { selectedDimIndex = it }
                )
            }
        }

        // 联动指标卡片
        val self = radarSelf[selectedDimIndex]
        val target = radarTarget[selectedDimIndex]
        val diff = self - target

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF009688).copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("维度能力评定 — ${radarDims[selectedDimIndex]}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = if (diff >= 0) "达标 (+${diff.toInt()})" else "待提升 (${diff.toInt()})",
                        color = if (diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("评定得分", fontSize = 11.sp, color = Color.Gray)
                        Text("${self.toInt()} 分", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                    }
                    Column {
                        Text("基准期望", fontSize = 11.sp, color = Color.Gray)
                        Text("${target.toInt()} 分", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                    }
                    Column {
                        Text("分值差距", fontSize = 11.sp, color = Color.Gray)
                        Text("${if (diff > 0) "+" else ""}${diff.toInt()} 分", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("能力评估", fontSize = 11.sp, color = Color.Gray)
                        Text(if (self >= 90) "专家级别" else if (self >= 80) "熟练骨干" else "发展成长", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E24AA))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "维度解析：${radarDims[selectedDimIndex]} 评定分值为 ${self.toInt()} 分，高于岗位基准要求 ${diff.toInt()} 分。",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ComposeRadarChartCore(
    modifier: Modifier = Modifier,
    dims: List<String>,
    selfScores: List<Float>,
    targetScores: List<Float>,
    selectedIndex: Int,
    onVertexSelected: (Int) -> Unit
) {
    val selfColor = Color(0xFF009688)
    val targetColor = Color(0xFFFFA000)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures {
                onVertexSelected((selectedIndex + 1) % dims.size)
            }
        }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) / 2.6f
        val vertexCount = dims.size
        val angleStep = (2 * Math.PI / vertexCount).toFloat()

        // 4 层蛛网
        for (layer in 1..4) {
            val radius = maxRadius * (layer / 4f)
            val webPath = Path()
            for (i in 0 until vertexCount) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)
                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            drawPath(path = webPath, color = Color.LightGray.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
        }

        // 轴线与文字
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        for (i in 0 until vertexCount) {
            val angle = i * angleStep - (Math.PI / 2).toFloat()
            val endX = center.x + maxRadius * cos(angle)
            val endY = center.y + maxRadius * sin(angle)
            drawLine(color = Color.LightGray.copy(alpha = 0.6f), start = center, end = Offset(endX, endY), strokeWidth = 1.dp.toPx())

            val labelX = center.x + (maxRadius + 24.dp.toPx()) * cos(angle)
            val labelY = center.y + (maxRadius + 16.dp.toPx()) * sin(angle)
            drawContext.canvas.nativeCanvas.drawText(dims[i], labelX, labelY + 10f, textPaint)
        }

        // 自我评定多边形
        val selfPath = Path()
        selfScores.forEachIndexed { i, score ->
            val r = maxRadius * (score / 100f)
            val angle = i * angleStep - (Math.PI / 2).toFloat()
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) selfPath.moveTo(x, y) else selfPath.lineTo(x, y)
        }
        selfPath.close()
        drawPath(path = selfPath, color = selfColor.copy(alpha = 0.35f), style = Fill)
        drawPath(path = selfPath, color = selfColor, style = Stroke(width = 2.5.dp.toPx()))

        // 团队基准多边形
        val targetPath = Path()
        targetScores.forEachIndexed { i, score ->
            val r = maxRadius * (score / 100f)
            val angle = i * angleStep - (Math.PI / 2).toFloat()
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) targetPath.moveTo(x, y) else targetPath.lineTo(x, y)
        }
        targetPath.close()
        drawPath(path = targetPath, color = targetColor.copy(alpha = 0.2f), style = Fill)
        drawPath(path = targetPath, color = targetColor, style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)))
    }
}
