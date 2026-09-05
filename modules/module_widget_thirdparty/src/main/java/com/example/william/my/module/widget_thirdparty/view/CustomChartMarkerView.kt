package com.example.william.my.module.widget_thirdparty.view

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.example.william.my.module.widget_thirdparty.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

/**
 * MPAndroidChart 自定义悬浮 Tooltip (MarkerView)
 */
@SuppressLint("ViewConstructor")
class CustomChartMarkerView(
    context: Context,
    layoutResource: Int = R.layout.widget_thirdparty_layout_chart_marker_view,
    private val xLabels: List<String>? = null,
) : MarkerView(context, layoutResource) {

    private val tvTitle: TextView = findViewById(R.id.tvMarkerTitle)
    private val tvContent: TextView = findViewById(R.id.tvMarkerContent)

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return

        val xIndex = e.x.toInt()
        val title = if (xLabels != null && xIndex in xLabels.indices) {
            xLabels[xIndex]
        } else {
            "数据项 #${xIndex + 1}"
        }

        tvTitle.text = title
        tvContent.text = "数值: ${e.y} 万元"

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // 让气泡居中显示在数据点上方
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}
