package com.example.william.my.core.base.ui.recycler.tab

import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder

/**
 * 可选中 Tab 的 RecyclerView Adapter 基类（维护当前/上次选中位置状态）
 */
abstract class RecyclerTabAdapter<T : Any> : BaseQuickAdapter<T, QuickViewHolder>() {

    protected var currentSelectPosition = 0
    protected var lastSelectPosition = 0

    fun setSelectPosition(currentSelectPosition: Int) {
        if (lastSelectPosition > -1) {
            notifyItemChanged(lastSelectPosition, false)
        }

        if (currentSelectPosition > -1) {
            notifyItemChanged(currentSelectPosition, true)
        }

        this.currentSelectPosition = currentSelectPosition
        this.lastSelectPosition = currentSelectPosition
    }
}
