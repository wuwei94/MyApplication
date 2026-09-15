package com.example.william.my.module.chart.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.chart.databinding.ChartActivityMpChartLinkageBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

/**
 * MPAndroidChart — 多图表全景联动看板 (Chart Linkage Dashboard)
 *
 * 演示同一页面内多个 MPAndroidChart 图表的联动编排：以折线图为主控制器，
 * 驱动柱状图与饼图随所选时间轴同步刷新，构建全景数据看板。
 *
 * 核心机制与避坑点：
 * 1. 主控折线图：顶部时间轴折线图作为控制器，Scrubbing / Tap 选择月份
 * 2. 子图柱状图：中部部门成本柱状图响应所选月份，动态重建 BarData 并播放动画
 * 3. 子图饼图：底部渠道获客饼图响应所选月份，按因子重算占比后重绘
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.Chart.MPChartLinkage)
class MPChartLinkageActivity : BaseVBActivity<ChartActivityMpChartLinkageBinding>() {

    private val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    private val revenueTrend = listOf(45f, 52f, 48f, 65f, 78f, 85f, 80f, 92f, 88f, 105f, 98f, 120f)

    private val departments = listOf("研发中心", "市场销售", "运营支持", "行政人事")
    private val channels = listOf("自然搜索", "社交媒体", "效果广告", "口碑转介绍")
    private val channelColors = listOf(
        Color.parseColor("#3B82F6"),
        Color.parseColor("#10B981"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#EC4899"),
    )

    override fun getViewBinding(): ChartActivityMpChartLinkageBinding = ChartActivityMpChartLinkageBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initMasterLineChart()
        initBarChart()
        initPieChart()

        updateSubCharts(5)
    }

    private fun initMasterLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(months)
                textColor = Color.GRAY
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 140f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                textColor = Color.GRAY
            }

            legend.isEnabled = false

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { updateSubCharts(it.x.toInt()) }
                }

                override fun onNothingSelected() {}
            })

            val entries = revenueTrend.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }
            val set = LineDataSet(entries, "营收").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = Color.parseColor("#3B82F6")
                lineWidth = 2.5f
                setCircleColor(Color.parseColor("#3B82F6"))
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#3B82F6")
                fillAlpha = 50
                setDrawValues(false)
            }

            data = LineData(set)
            animateX(600, Easing.EaseInOutQuad)
        }
    }

    private fun initBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(departments)
                textColor = Color.GRAY
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.GRAY
            }
        }
    }

    private fun initPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 55f
            transparentCircleRadius = 60f
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSubCharts(monthIdx: Int) {
        if (monthIdx !in months.indices) return
        val monthName = months[monthIdx]
        val factor = 1.0f + (monthIdx * 0.08f)

        // 1. 刷新柱状图
        binding.tvBarTitle.text = "$monthName 部门成本支出 (万元)"
        val deptCosts = listOf(18f * factor, 12f * factor, 8f * factor, 5f * factor)
        val barEntries = deptCosts.mapIndexed { idx, v -> BarEntry(idx.toFloat(), v) }
        val barSet = BarDataSet(barEntries, "成本").apply {
            color = Color.parseColor("#10B981")
            valueTextSize = 10f
        }
        binding.barChart.data = BarData(barSet)
        binding.barChart.animateY(400)
        binding.barChart.invalidate()

        // 2. 刷新饼图
        binding.tvPieTitle.text = "$monthName 获客渠道构成占比"
        val channelShares = listOf(35f + monthIdx % 5, 25f - monthIdx % 3, 20f + monthIdx % 4, 20f - monthIdx % 2)
        val pieEntries = channelShares.mapIndexed { idx, v -> PieEntry(v, channels[idx]) }
        val pieSet = PieDataSet(pieEntries, "").apply {
            colors = channelColors
            sliceSpace = 2f
            valueFormatter = PercentFormatter(binding.pieChart)
            valueTextSize = 10f
            valueTextColor = Color.WHITE
        }
        binding.pieChart.data = PieData(pieSet)
        binding.pieChart.centerText = "$monthName\n渠道"
        binding.pieChart.animateY(400)
        binding.pieChart.invalidate()
    }
}
