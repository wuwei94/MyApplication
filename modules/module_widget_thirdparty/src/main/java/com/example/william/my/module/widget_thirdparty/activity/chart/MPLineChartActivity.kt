package com.example.william.my.module.widget_thirdparty.activity.chart

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityMpLineChartBinding
import com.example.william.my.module.widget_thirdparty.view.CustomChartMarkerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

/**
 * MPAndroidChart — 折线图 (LineChart)
 *
 * 核心特性：
 * 1. 双曲线收支对比、Cubic 贝塞尔平滑曲线
 * 2. 渐变面积填充与数据圆点高亮
 * 3. 自定义 MarkerView 悬浮气泡 Tooltip
 * 4. OnChartValueSelectedListener 联动底部指标看板
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.WidgetThirdparty.MPLineChart)
class MPLineChartActivity : BaseVBActivity<WidgetThirdpartyActivityMpLineChartBinding>() {

    private val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    private val incomeData = listOf(28.5f, 35.2f, 31.0f, 42.8f, 48.6f, 55.0f, 51.2f, 63.4f, 59.8f, 72.0f, 68.5f, 84.2f)
    private val expenseData = listOf(18.0f, 22.4f, 26.8f, 28.0f, 34.5f, 38.2f, 35.0f, 41.5f, 39.0f, 46.2f, 44.0f, 52.6f)

    override fun getViewBinding(): WidgetThirdpartyActivityMpLineChartBinding {
        return WidgetThirdpartyActivityMpLineChartBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initLineChart()
        updateMetrics(5)
    }

    private fun initLineChart() {
        mBinding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)

            val marker = CustomChartMarkerView(this@MPLineChartActivity, xLabels = months)
            marker.chartView = this
            this.marker = marker

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
                axisMaximum = 100f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                textColor = Color.GRAY
            }

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { updateMetrics(it.x.toInt()) }
                }

                override fun onNothingSelected() {}
            })

            val incomeEntries = incomeData.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }
            val expenseEntries = expenseData.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }

            val incomeSet = LineDataSet(incomeEntries, "收入").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = Color.parseColor("#00897B")
                lineWidth = 2.5f
                setCircleColor(Color.parseColor("#00897B"))
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#00897B")
                fillAlpha = 50
                setDrawValues(false)
            }

            val expenseSet = LineDataSet(expenseEntries, "支出").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = Color.parseColor("#E64A19")
                lineWidth = 2.5f
                setCircleColor(Color.parseColor("#E64A19"))
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleRadius = 2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#E64A19")
                fillAlpha = 40
                setDrawValues(false)
            }

            data = LineData(incomeSet, expenseSet)
            animateX(800, Easing.EaseInOutQuad)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetrics(index: Int) {
        if (index !in months.indices) return
        val inc = incomeData[index]
        val exp = expenseData[index]
        val profit = inc - exp
        val margin = (profit / inc) * 100

        mBinding.tvMetricsTitle.text = "选中月份数据联动 (${months[index]})"
        mBinding.tvBadgeStatus.text = if (profit >= 0) "盈利良好" else "支出预警"
        mBinding.tvBadgeStatus.backgroundTintList = ColorStateList.valueOf(
            if (profit >= 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        )

        mBinding.tvIncome.text = "${inc} 万"
        mBinding.tvExpense.text = "${exp} 万"
        mBinding.tvProfit.text = "${if (profit > 0) "+" else ""}${String.format("%.1f", profit)} 万"
        mBinding.tvMargin.text = "${String.format("%.1f", margin)}%"
    }
}
