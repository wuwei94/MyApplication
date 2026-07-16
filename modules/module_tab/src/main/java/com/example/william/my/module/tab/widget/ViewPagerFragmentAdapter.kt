package com.example.william.my.module.tab.widget

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * ViewPager 的 Fragment 适配器。
 *
 * 使用 [FragmentStatePagerAdapter]，
 * destroyItem 时会 remove Fragment（而非 detach），支持 setAdapter 整体刷新。
 * behavior 设为 [BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT]，
 * 当前页面 Fragment 处于 RESUMED，其余限制在 STARTED，天然支持懒加载。
 */
class ViewPagerFragmentAdapter(
    fm: FragmentManager,
    private val mFragments: List<Fragment>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = mFragments.size

    override fun getItem(position: Int): Fragment = mFragments[position]
}
