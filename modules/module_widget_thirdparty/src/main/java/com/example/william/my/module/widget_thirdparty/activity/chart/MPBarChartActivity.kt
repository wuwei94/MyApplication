package com.example.william.my.module.widget_thirdparty.activity.chart

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityMpBarChartBinding
import com.example.william.my.module.widget_thirdparty.view.CustomChartMarkerView
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
 * 核心特性：
 * 1. 季度目标 vs 实际销售额分组柱状图
 * 2. X 轴季度标签格式化与居中对齐
 * 3. 柱体触摸高亮与 MarkerView 提示
 * 4. 实时联动底部业绩达成率与差额分析
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.WidgetThirdparty.MPBarChart)
class MPBarChartActivity : BaseVBActivity<WidgetThirdpartyActivityMpBarChartBinding>() {

    private val quarters = listOf("Q1 第一季度", "Q2 第二季度", "Q3 第三季度", "Q4 第四季度")
    private val targetSales = listOf(120f, 150f, 180f, 220f)
    private val actualSales = listOf(135.5f, 142f, 210.8f, 245f)

    override fun getViewBinding(): WidgetThirdpartyActivityMpBarChartBinding {
        return WidgetThirdpartyActivityMpBarChartBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBarChart()
        updateMetrics(2)
    }

    private fun initBarChart() {
        mBinding.barChart.apply {
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

        mBinding.tvMetricsTitle.text = "${quarters[index]} 业绩达成分析"
        mBinding.tvBadgeStatus.text = if (rate >= 100) "超额完成" else "未达预期"
        mBinding.tvBadgeStatus.backgroundTintList = ColorStateList.valueOf(
            if (rate >= 100) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        )

        mBinding.tvTarget.text = "${target} 万"
        mBinding.tvActual.text = "${actual} 万"
        mBinding.tvDiff.text = "${if (diff > 0) "+" else ""}${String.format("%.1f", diff)} 万"
        mBinding.tvRate.text = "${String.format("%.1f", rate)}%"
        mBinding.tvRemark.text = "分析：Q${index + 1} 实际营收 ${if (diff >= 0) "超过" else "低于"} 目标 ${abs(diff)} 万元"
    }
}
