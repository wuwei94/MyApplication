package com.example.william.my.module.feature.layoutmanager

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**
 * 麦克风布局管理器
 *
 * 自定义 RecyclerView 布局管理器，实现麦克风波形的排列。
 */
class MicLayoutManager : RecyclerView.LayoutManager() {

    /**
     * 布局模式
     *
     * 麦克风布局的排列模式。
     */
    enum class LayoutMode {
        MODE_A,
        MODE_B,
    }

    var layoutMode: LayoutMode = LayoutMode.MODE_A
        private set

    fun switchMode(mode: LayoutMode): Boolean {
        if (layoutMode == mode) return false
        layoutMode = mode
        requestLayout()
        return true
    }

    override fun isAutoMeasureEnabled(): Boolean = false

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams = RecyclerView.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int,
    ) {
        val width = View.MeasureSpec.getSize(widthSpec)

        if (width > 0) {
            val itemSize = width / 4
            val rowGap = itemSize / 2
            val rows = if (layoutMode == LayoutMode.MODE_A) 2 else 4
            val totalHeight = rows * itemSize + (rows - 1) * rowGap
            setMeasuredDimension(width, totalHeight)
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)

        val w = width
        if (w == 0) return

        val itemSize = w / 4
        val rowGap = itemSize / 2

        when (layoutMode) {
            LayoutMode.MODE_A -> layoutModeA(recycler, itemSize, rowGap)
            LayoutMode.MODE_B -> layoutModeB(recycler, itemSize, rowGap)
        }
    }

    /**
     * 状态一:
     * [1][2][3][4]
     * [5][6][7][8]
     */
    private fun layoutModeA(recycler: RecyclerView.Recycler, itemSize: Int, rowGap: Int) {
        val positions = arrayOf(
            intArrayOf(0, 0),
            intArrayOf(itemSize, 0),
            intArrayOf(2 * itemSize, 0),
            intArrayOf(3 * itemSize, 0),
            intArrayOf(0, itemSize + rowGap),
            intArrayOf(itemSize, itemSize + rowGap),
            intArrayOf(2 * itemSize, itemSize + rowGap),
            intArrayOf(3 * itemSize, itemSize + rowGap),
        )
        layoutAll(recycler, positions, itemSize)
    }

    /**
     * 状态二:
     * [1][5]
     * [2][6]
     * [3][7]
     * [4][8]
     */
    private fun layoutModeB(recycler: RecyclerView.Recycler, itemSize: Int, rowGap: Int) {
        val colGap = itemSize / 2
        val colsWidth = 2 * itemSize + colGap
        val col0 = (width - colsWidth) / 2
        val col1 = col0 + itemSize + colGap

        val positions = arrayOf(
            intArrayOf(col0, 0),
            intArrayOf(col0, itemSize + rowGap),
            intArrayOf(col0, 2 * (itemSize + rowGap)),
            intArrayOf(col0, 3 * (itemSize + rowGap)),
            intArrayOf(col1, 0),
            intArrayOf(col1, itemSize + rowGap),
            intArrayOf(col1, 2 * (itemSize + rowGap)),
            intArrayOf(col1, 3 * (itemSize + rowGap)),
        )
        layoutAll(recycler, positions, itemSize)
    }

    private fun layoutAll(
        recycler: RecyclerView.Recycler,
        positions: Array<IntArray>,
        itemSize: Int,
    ) {
        for (i in 0 until min(itemCount, positions.size)) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            val lp = view.layoutParams as RecyclerView.LayoutParams
            lp.width = itemSize
            lp.height = itemSize
            val widthSpec = View.MeasureSpec.makeMeasureSpec(itemSize, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(itemSize, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            val left = positions[i][0]
            val top = positions[i][1]
            layoutDecorated(view, left, top, left + itemSize, top + itemSize)
        }
    }

    override fun canScrollVertically(): Boolean = false
    override fun canScrollHorizontally(): Boolean = false
}
