package com.example.william.my.core.base.ui.recycler.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 顶边距装饰器（仅首行设置上间距）
 */
class RItemDecorationEdgeTop(
    private val marginTop: Int,
) : RItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val itemCount = getItemCount(parent)
        val spanCount = getSpanCount(parent)

        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount // 第几列
        val row = position / spanCount // 第几行

        if (position == RecyclerView.NO_POSITION) return

        if (position < spanCount) {
            outRect.top = marginTop
        }
    }
}
