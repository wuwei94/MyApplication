package com.example.william.my.module.widget_thirdparty.activity.chart

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityMpPieChartBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

/**
 * MPAndroidChart — 饼图/环形甜甜圈图 (PieChart)
 *
 * 核心特性：
 * 1. 环形甜甜圈 (HoleRadius) 与中心数据标注
 * 2. 扇区点击触发外扩动画 (selectionShift)
 * 3. 百分比数值格式化与图例展示
 * 4. 实时联动底部品类明细与预算占比
 *
 * https://github.com/PhilJay/MPAndroidChart
 */
@Route(path = RouterPath.WidgetThirdparty.MPPieChart)
class MPPieChartActivity : BaseVBActivity<WidgetThirdpartyActivityMpPieChartBinding>() {

    private val pieCategories = listOf("云计算研发", "市场营销", "人力薪酬", "办公行政", "流动储备")
    private val pieAmounts = listOf(45.8f, 32.5f, 68.2f, 18.0f, 25.5f)
    private val pieColors = listOf(
        Color.parseColor("#3B82F6"),
        Color.parseColor("#10B981"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#EC4899"),
        Color.parseColor("#8B5CF6")
    )

    override fun getViewBinding(): WidgetThirdpartyActivityMpPieChartBinding {
        return WidgetThirdpartyActivityMpPieChartBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPieChart()
        updateMetrics(0)
    }

    private fun initPieChart() {
        mBinding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 62f
            setDrawCenterText(true)
            centerText = "总成本\n190.0 万"
            setCenterTextSize(14f)
            setCenterTextColor(Color.DKGRAY)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val pieEntry = it as? PieEntry ?: return
                        val idx = pieCategories.indexOf(pieEntry.label)
                        if (idx >= 0) updateMetrics(idx)
                    }
                }

                override fun onNothingSelected() {}
            })

            val entries = pieCategories.mapIndexed { idx, name ->
                PieEntry(pieAmounts[idx], name)
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = pieColors
                sliceSpace = 3f
                selectionShift = 8f
                valueFormatter = PercentFormatter(mBinding.pieChart)
                valueTextSize = 11f
                valueTextColor = Color.WHITE
            }

            data = PieData(dataSet)
            animateY(800, Easing.EaseInOutQuad)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetrics(index: Int) {
        if (index !in pieCategories.indices) return
        val name = pieCategories[index]
        val amount = pieAmounts[index]
        val total = pieAmounts.sum()
        val pct = (amount / total) * 100

        mBinding.tvMetricsTitle.text = "品类成本细分 — $name"
        mBinding.tvBadgeStatus.text = "占比 ${String.format("%.1f", pct)}%"
        mBinding.tvBadgeStatus.backgroundTintList = ColorStateList.valueOf(pieColors[index])

        mBinding.tvCategoryName.text = name
        mBinding.tvAmount.text = "${amount} 万"
        mBinding.tvTotal.text = "${String.format("%.1f", total)} 万"
        mBinding.tvPct.text = "${String.format("%.1f", pct)}%"
    }
}
