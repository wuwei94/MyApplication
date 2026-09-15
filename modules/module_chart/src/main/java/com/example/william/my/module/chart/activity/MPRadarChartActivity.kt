package com.example.william.my.module.chart.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.chart.databinding.ChartActivityMpRadarChartBinding
import com.example.william.my.module.chart.view.CustomChartMarkerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

/**
 * MPAndroidChart — 六维雷达能力评估图 (RadarChart)
 *
 * RadarChart 是 MPAndroidChart 的雷达图（蛛网图）视图，适合多维度能力/指标的对比评估。
 * 本页演示自我评定 vs 团队基准的六维雷达对比，并联动底部维度解析看板。
 *
 * 核心机制与避坑点：
 * 1. 蛛网多边形：webLineWidth / webColor 自绘正多边形网格与刻度
 * 2. 双数据集叠加：两个 RadarDataSet 半透明填充重叠，直观对比两组数据
 * 3. 顶点触控：setDrawHighlightCircleEnabled + MarkerView 弹窗展示选中顶点
 * 4. 维度联动：OnChartValueSelectedListener 响应顶点选中，刷新底部差距分析
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.Chart.MPRadarChart)
class MPRadarChartActivity : BaseVBActivity<ChartActivityMpRadarChartBinding>() {

    private val radarDims = listOf("架构设计", "性能优化", "源码理解", "跨端实战", "工程运维", "团队协作")
    private val radarSelf = listOf(92f, 88f, 85f, 95f, 78f, 89f)
    private val radarTarget = listOf(85f, 80f, 90f, 85f, 75f, 80f)

    override fun getViewBinding(): ChartActivityMpRadarChartBinding = ChartActivityMpRadarChartBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRadarChart()
        updateMetrics(0)
    }

    private fun initRadarChart() {
        binding.radarChart.apply {
            description.isEnabled = false
            webLineWidth = 1.5f
            webColor = Color.LTGRAY
            webLineWidthInner = 1f
            webColorInner = Color.LTGRAY
            webAlpha = 100

            val marker = CustomChartMarkerView(this@MPRadarChartActivity, xLabels = radarDims)
            marker.chartView = this
            this.marker = marker

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(radarDims)
                textSize = 11f
                textColor = Color.DKGRAY
            }

            yAxis.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawLabels(false)
                setLabelCount(5, false)
            }

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val dimIdx = it.x.toInt().coerceIn(0, radarDims.size - 1)
                        updateMetrics(dimIdx)
                    }
                }

                override fun onNothingSelected() {}
            })

            val selfEntries = radarSelf.map { RadarEntry(it) }
            val targetEntries = radarTarget.map { RadarEntry(it) }

            val selfSet = RadarDataSet(selfEntries, "自我评定").apply {
                color = Color.parseColor("#009688")
                fillColor = Color.parseColor("#009688")
                setDrawFilled(true)
                fillAlpha = 120
                lineWidth = 2f
                setDrawHighlightCircleEnabled(true)
                setDrawValues(false)
            }

            val targetSet = RadarDataSet(targetEntries, "团队基准").apply {
                color = Color.parseColor("#FFA000")
                fillColor = Color.parseColor("#FFA000")
                setDrawFilled(true)
                fillAlpha = 80
                lineWidth = 1.5f
                setDrawHighlightCircleEnabled(true)
                setDrawValues(false)
            }

            data = RadarData(selfSet, targetSet)
            animateXY(800, 800, Easing.EaseInOutQuad)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetrics(index: Int) {
        if (index !in radarDims.indices) return
        val name = radarDims[index]
        val self = radarSelf[index]
        val target = radarTarget[index]
        val diff = self - target

        binding.tvMetricsTitle.text = "维度能力评定 — $name"
        binding.tvBadgeStatus.text = if (diff >= 0) "达标 (+${diff.toInt()})" else "待提升 (${diff.toInt()})"
        binding.tvBadgeStatus.backgroundTintList = ColorStateList.valueOf(
            if (diff >= 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444"),
        )

        binding.tvScore.text = "${self.toInt()} 分"
        binding.tvTargetScore.text = "${target.toInt()} 分"
        binding.tvGap.text = "${if (diff > 0) "+" else ""}${diff.toInt()} 分"
        binding.tvLevel.text = if (self >= 90) {
            "专家级别"
        } else if (self >= 80) {
            "熟练骨干"
        } else {
            "发展成长"
        }
        binding.tvRemark.text = "维度解析：$name 评定分值为 ${self.toInt()} 分，高于岗位基准要求 ${diff.toInt()} 分。"
    }
}
