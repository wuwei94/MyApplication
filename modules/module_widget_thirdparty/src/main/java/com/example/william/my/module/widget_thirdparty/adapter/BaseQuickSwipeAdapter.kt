package com.example.william.my.module.widget_thirdparty.adapter

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes

abstract class BaseQuickSwipeAdapter<T : Any, VH : RecyclerView.ViewHolder> :
    BaseQuickAdapter<T, VH>(),
    SwipeItemMangerInterface,
    SwipeAdapterInterface {

    private var itemManger: SwipeItemRecyclerMangerImpl = SwipeItemRecyclerMangerImpl(this)

    override fun onBindViewHolder(holder: VH, position: Int, item: T?) {
        if (holder.itemView is SwipeLayout) {
            itemManger.bindView(holder.itemView, position)
        }
    }

    override fun openItem(position: Int) {
        itemManger.openItem(position)
    }

    override fun closeItem(position: Int) {
        itemManger.closeItem(position)
    }

    override fun closeAllExcept(layout: SwipeLayout?) {
        itemManger.closeAllExcept(layout)
    }

    override fun closeAllItems() {
        itemManger.closeAllItems()
    }

    override fun getOpenItems(): MutableList<Int?>? = itemManger.openItems

    override fun getOpenLayouts(): MutableList<SwipeLayout?>? = itemManger.openLayouts

    override fun removeShownLayouts(layout: SwipeLayout?) {
        itemManger.removeShownLayouts(layout)
    }

    override fun isOpen(position: Int): Boolean = itemManger.isOpen(position)

    override fun getMode(): Attributes.Mode? = itemManger.mode

    override fun setMode(mode: Attributes.Mode?) {
        itemManger.setMode(mode)
    }
}
