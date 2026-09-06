package com.example.william.my.module.performance.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecycledViewPool 演示页专属 ViewPager2 适配器
 *
 * 负责构建每个 Tab 页面的子 RecyclerView，并统一绑定全局共享的 [sharedViewPool]。
 */
class PoolPagerAdapter(
    private val sharedViewPool: RecyclerView.RecycledViewPool,
    private val tabCount: Int = 2,
    private val onCreateItem: (isTab1: Boolean) -> Unit,
    private val onBindItem: (isTab1: Boolean) -> Unit,
) : RecyclerView.Adapter<PoolPagerAdapter.PageViewHolder>() {

    override fun getItemCount(): Int = tabCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val recyclerView = RecyclerView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            layoutManager = LinearLayoutManager(parent.context)
            clipToPadding = false
            setPadding(0, 0, 0, 32)
            // 核心关键：为每个子 RecyclerView 绑定同一个共享的 RecycledViewPool
            setRecycledViewPool(sharedViewPool)
        }
        return PageViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val isTab1 = position == 0
        val data = (1..60).map { "商品卡片 #$it" }

        val adapter = PoolItemAdapter(
            tabName = if (isTab1) "Tab 1" else "Tab 2",
            items = data,
            onCreateCallback = { onCreateItem(isTab1) },
            onBindCallback = { onBindItem(isTab1) },
        )
        holder.recyclerView.adapter = adapter
    }

    /**
     * 分页 ViewHolder
     *
     * 承载子 RecyclerView 的页面容器。
     */
    class PageViewHolder(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)
}
