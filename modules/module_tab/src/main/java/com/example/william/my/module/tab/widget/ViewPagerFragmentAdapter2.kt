package com.example.william.my.module.tab.widget

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2 的 Fragment 适配器。
 *
 * 使用 [FragmentStateAdapter]，内部基于 RecyclerView 实现，
 * 根据 [Lifecycle] 自动管理 Fragment 状态，无需手动处理懒加载。
 */
class ViewPagerFragmentAdapter2(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val mFragments: List<Fragment>
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = mFragments.size

    override fun createFragment(position: Int): Fragment = mFragments[position]
}
