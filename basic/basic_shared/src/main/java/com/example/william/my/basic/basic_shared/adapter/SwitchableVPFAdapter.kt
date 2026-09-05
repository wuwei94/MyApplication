package com.example.william.my.basic.basic_shared.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * 可切换 behavior 的 ViewPager Fragment 适配器
 *
 * 通过 isNew 参数切换懒加载行为：
 * - isNew = true:  使用 BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT（推荐）
 *   只有当前 Fragment 执行 onResume()，其他限制在 onStart()
 * - isNew = false: 使用 BEHAVIOR_SET_USER_VISIBLE_HINT（旧方式）
 *   通过 setUserVisibleHint() 控制懒加载
 *
 * FragmentStatePagerAdapter: destroy 时销毁 Fragment 和 View，适合大量页面，节省内存
 */
@Suppress("DEPRECATION")
class SwitchableVPFAdapter(
    fm: FragmentManager,
    private val mFragments: List<Fragment> = emptyList(),
    private val mTitles: List<String>? = null,
    isNew: Boolean,
) : FragmentStatePagerAdapter(
    fm,
    if (isNew) BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT else BEHAVIOR_SET_USER_VISIBLE_HINT,
) {

    override fun getCount(): Int = mFragments.size

    override fun getItem(position: Int): Fragment = mFragments[position]

    override fun getPageTitle(position: Int): CharSequence = mTitles?.getOrNull(position) ?: ""
}
