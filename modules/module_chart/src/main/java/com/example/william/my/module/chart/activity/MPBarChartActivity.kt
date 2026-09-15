package com.example.william.my.module.chart.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.chart.databinding.ChartActivityMpBarChartBinding
import com.example.william.my.module.chart.view.CustomChartMarkerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlin.math.abs

/**
 * MPAndroidChart — 柱状图 (BarChart)
 *
 * BarChart 是 MPAndroidChart 的柱状图视图，支持单组/分组柱体、动画与触摸高亮。
 * 本页演示季度目标 vs 实际销售额的分组柱状图，并联动底部业绩分析看板。
 *
 * 核心机制与避坑点：
 * 1. 分组柱状图：groupBars() 将多组 BarDataSet 并排展示，groupSpace/barSpace 控制间距
 * 2. X 轴格式化：IndexAxisValueFormatter 绑定季度标签，setCenterAxisLabels 居中对齐
 * 3. 触摸高亮：OnChartValueSelectedListener 响应选中，自定义 MarkerView 悬浮提示
 * 4. 数据联动：选中柱体后实时刷新底部达成率、差额与文字分析
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.Chart.MPBarChart)
class MPBarChartActivity : BaseVBActivity<ChartActivityMpBarChartBinding>() {

    private val quarters = listOf("Q1 第一季度", "Q2 第二季度", "Q3 第三季度", "Q4 第四季度")
    private val targetSales = listOf(120f, 150f, 180f, 220f)
    private val actualSales = listOf(135.5f, 142f, 210.8f, 245f)

    override fun getViewBinding(): ChartActivityMpBarChartBinding = ChartActivityMpBarChartBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBarChart()
        updateMetrics(2)
    }

    private fun initBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)

            val marker = CustomChartMarkerView(this@MPBarChartActivity, xLabels = quarters)
            marker.chartView = this
            this.marker = marker

            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                axisMinimum = 0f
                axisMaximum = quarters.size.toFloat()
                setCenterAxisLabels(true)
                valueFormatter = IndexAxisValueFormatter(listOf("Q1", "Q2", "Q3", "Q4"))
                textColor = Color.GRAY
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 300f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                textColor = Color.GRAY
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val quarterIdx = it.x.toInt().coerceIn(0, quarters.size - 1)
                        updateMetrics(quarterIdx)
                    }
                }

                override fun onNothingSelected() {}
            })

            val targetEntries = targetSales.mapIndexed { idx, v -> BarEntry(idx.toFloat(), v) }
            val actualEntries = actualSales.mapIndexed { idx, v -> BarEntry(idx.toFloat(), v) }

            val targetSet = BarDataSet(targetEntries, "目标销售额").apply {
                color = Color.parseColor("#3F51B5")
                setDrawValues(false)
            }

            val actualSet = BarDataSet(actualEntries, "实际销售额").apply {
                color = Color.parseColor("#009688")
                setDrawValues(false)
            }

            val barData = BarData(targetSet, actualSet).apply {
                this.barWidth = barWidth
            }

            data = barData
            groupBars(0f, groupSpace, barSpace)
            animateY(800, Easing.EaseInOutQuad)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetrics(index: Int) {
        if (index !in quarters.indices) return
        val target = targetSales[index]
        val actual = actualSales[index]
        val diff = actual - target
        val rate = (actual / target) * 100

        binding.tvMetricsTitle.text = "${quarters[index]} 业绩达成分析"
        binding.tvBadgeStatus.text = if (rate >= 100) "超额完成" else "未达预期"
        binding.tvBadgeStatus.backgroundTintList = ColorStateList.valueOf(
            if (rate >= 100) Color.parseColor("#10B981") else Color.parseColor("#EF4444"),
        )

        binding.tvTarget.text = "$target 万"
        binding.tvActual.text = "$actual 万"
        binding.tvDiff.text = "${if (diff > 0) "+" else ""}${String.format("%.1f", diff)} 万"
        binding.tvRate.text = "${String.format("%.1f", rate)}%"
        binding.tvRemark.text = "分析：Q${index + 1} 实际营收 ${if (diff >= 0) "超过" else "低于"} 目标 ${abs(diff)} 万元"
    }
}
