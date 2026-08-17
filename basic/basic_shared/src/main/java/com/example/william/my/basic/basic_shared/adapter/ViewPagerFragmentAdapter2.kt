package com.example.william.my.basic.basic_shared.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2 的 Fragment 适配器
 *
 * FragmentStateAdapter: 基于 RecyclerView 实现，根据 Lifecycle 自动管理 Fragment 状态
 *
 * @param fm FragmentManager
 * @param lifecycle Activity/Fragment 的 Lifecycle
 * @param mFragments Fragment 列表
 * @param mTitles 可选的标题列表，配合 TabLayoutMediator 使用
 */
class ViewPagerFragmentAdapter2(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val mFragments: List<Fragment> = emptyList(),
    private val mTitles: List<String>? = null
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = mFragments.size

    override fun createFragment(position: Int): Fragment = mFragments[position]

    fun getPageTitle(position: Int): CharSequence? {
        return mTitles?.getOrNull(position)
    }
}
