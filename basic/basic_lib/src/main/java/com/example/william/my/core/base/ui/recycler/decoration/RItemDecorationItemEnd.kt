package com.example.william.my.core.base.ui.recycler.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 右侧间距装饰器（每项设置右边距，可选是否包含末项）
 */
class RItemDecorationItemEnd(
    private val space: Int,
    private val includeEnd: Boolean = true, // 是否包含最后一个
) : RItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val itemCount = getItemCount(parent)
        val spanCount = getSpanCount(parent)

        val column = position % spanCount // 第几列
        val row = position / spanCount // 第几行

        if (includeEnd) {
            outRect.right = space
        } else if (position != itemCount - 1) {
            outRect.right = space
        }
    }
}
