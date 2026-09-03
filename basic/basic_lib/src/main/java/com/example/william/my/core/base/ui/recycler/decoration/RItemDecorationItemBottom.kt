package com.example.william.my.core.base.ui.recycler.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 下间距(每行)
 */
class RItemDecorationItemBottom(
    private val space: Int,
    private val includeBottom: Boolean = false, // 是否包含底部
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
        if (spanCount <= 0) return

        val column = position % spanCount // 第几列
        val row = position / spanCount // 第几行

        if (includeBottom) {
            outRect.bottom = space
        } else if (itemCount > 0 && row != (itemCount - 1) / spanCount) {
            outRect.bottom = space
        }
    }
}
