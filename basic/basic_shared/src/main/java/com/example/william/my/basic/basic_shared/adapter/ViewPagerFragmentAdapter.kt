package com.example.william.my.basic.basic_shared.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * ViewPager 的 Fragment 适配器
 *
 * FragmentStatePagerAdapter: destroy 时销毁 Fragment 和 View，适合大量页面，节省内存
 * 使用 BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT：只有当前 Fragment 执行 onResume()，其他限制在 onStart()
 * 使用 BEHAVIOR_SET_USER_VISIBLE_HINT：通过 setUserVisibleHint() 控制懒加载
 *
 * @param fm FragmentManager
 * @param mFragments Fragment 列表
 * @param mTitles 可选的标题列表，用于 TabLayout 显示
 */
@Suppress("deprecation")
class ViewPagerFragmentAdapter(
    fm: FragmentManager,
    private val mFragments: List<Fragment> = emptyList(),
    private val mTitles: List<String>? = null,
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = mFragments.size

    override fun getItem(position: Int): Fragment = mFragments[position]

    override fun getPageTitle(position: Int): CharSequence = mTitles?.getOrNull(position) ?: ""
}
