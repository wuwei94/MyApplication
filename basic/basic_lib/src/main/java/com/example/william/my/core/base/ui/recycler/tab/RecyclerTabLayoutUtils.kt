@file:JvmName("RecyclerTabLayoutUtils")

package com.example.william.my.core.base.ui.recycler.tab

import androidx.viewpager2.widget.ViewPager2

/**
 * 将 RecyclerTabAdapter 点击事件与 ViewPager2 页面切换进行联动
 */
fun RecyclerTabAdapter<*>.setupWithViewPager(viewPager: ViewPager2) {
    this.setOnItemClickListener { _, _, position ->
        viewPager.currentItem = position
    }
}

/**
 * 将 ViewPager2 页面滑动事件同步至 RecyclerTabAdapter 的选中状态
 */
fun ViewPager2.setupWithTabAdapter(adapter: RecyclerTabAdapter<*>) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            adapter.setSelectPosition(position)
        }
    })
}

/**
 * 兼容性别名方法：将 TabAdapter 与 ViewPager2 绑定
 */
fun RecyclerTabAdapter<*>.setViewPager(viewPager: ViewPager2) {
    setupWithViewPager(viewPager)
}

/**
 * 兼容性别名方法：为 ViewPager2 绑定 TabAdapter
 */
fun ViewPager2.setTabAdapter(adapter: RecyclerTabAdapter<*>) {
    setupWithTabAdapter(adapter)
}
