package com.example.william.my.module.gpuimage.helper

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 滤镜选择 Chip 构建工具
 *
 * 三个演示页共用同一套「横向可滚动滤镜条」交互：代码动态生成圆角 TextView，
 * 点击时高亮选中项并回调下标。将重复逻辑收敛在此，页面只负责数据与业务回调。
 */
object GpuImageChipHelper {

    private const val CHIP_SELECTED = 0xFF1976D2.toInt()
    private const val CHIP_UNSELECTED = 0xFFECEFF1.toInt()
    private const val TEXT_SELECTED = 0xFFFFFFFF.toInt()
    private const val TEXT_UNSELECTED = 0xFF212121.toInt()

    /**
     * 用 [names] 填充 [container] 并注册点击回调。
     *
     * @param initialIndex 默认选中下标
     * @param onSelect 选中变化回调（传入新的下标）
     */
    fun populate(
        container: LinearLayout,
        names: List<String>,
        initialIndex: Int,
        onSelect: (index: Int) -> Unit,
    ) {
        container.removeAllViews()
        val context = container.context
        names.forEachIndexed { index, name ->
            val chip = TextView(context).apply {
                text = name
                textSize = 13f
                setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6))
                setChipStyle(context, index == initialIndex)
                setOnClickListener {
                    // 更新全部 chip 的选中态后回调
                    for (i in 0 until container.childCount) {
                        (container.getChildAt(i) as TextView).setChipStyle(context, i == index)
                    }
                    onSelect(index)
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginEnd = dp(context, 8)
            container.addView(chip, params)
        }
    }

    private fun TextView.setChipStyle(context: Context, selected: Boolean) {
        background = GradientDrawable().apply {
            cornerRadius = dp(context, 16).toFloat()
            setColor(if (selected) CHIP_SELECTED else CHIP_UNSELECTED)
        }
        setTextColor(if (selected) TEXT_SELECTED else TEXT_UNSELECTED)
    }
}

/** dp 转 px */
private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
