package com.example.william.my.core.base.ui.recycler.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RItemDecorationItemSpacing(
    private var spacing: Int,
    private var includeEdge: Boolean = false,
    private var bottom: Int = 0,
    private var includeBottom: Boolean = false,
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

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
        }

        if (includeBottom) {
            outRect.bottom = bottom
        } else if (itemCount > 0 && row != (itemCount - 1) / spanCount) {
            outRect.bottom = bottom
        }
    }
}
