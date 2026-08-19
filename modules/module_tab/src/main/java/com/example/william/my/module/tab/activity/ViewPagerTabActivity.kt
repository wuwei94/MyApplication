package com.example.william.my.module.tab.activity

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityViewPagerTabBinding

/**
 * ViewPager + Tab — ViewPager 实现 Tab 切换
 *
 * 使用 ViewPager + RadioGroup 实现 Tab 切换，支持 Fragment 页面切换。
 *
 * 核心特性：
 * 1. ViewPager 联动：Tab 和 ViewPager 联动切换
 * 2. Fragment 支持：支持 Fragment 页面切换
 * 3. 自定义样式：支持自定义 Tab 样式
 * 4. 图标支持：支持图标和文字组合
 *
 * 基本用法：
 * ```kotlin
 * // 设置 ViewPager 适配器
 * viewPager.adapter = ViewPagerFragmentAdapter(supportFragmentManager, fragments)
 *
 * // 设置 RadioGroup 监听
 * radioGroup.setOnCheckedChangeListener { group, checkedId ->
 *     // 切换 ViewPager
 *     viewPager.currentItem = position
 * }
 * ```
 *
 * 适用场景：
 * - Tab 切换
 * - 底部导航栏
 * - 多 Tab 页面切换
 */
@Route(path = RouterPath.Tab.ViewPagerTab)
class ViewPagerTabActivity : BaseVBActivity<TabActivityViewPagerTabBinding>(),
    RadioGroup.OnCheckedChangeListener {

    override fun getViewBinding(): TabActivityViewPagerTabBinding {
        return TabActivityViewPagerTabBinding.inflate(layoutInflater)
    }

    private val mTitles: ArrayList<String> by lazy {
        arrayListOf(
            getString(R.string.tab_title_home),
            getString(R.string.tab_title_discover),
            getString(R.string.tab_title_message),
            getString(R.string.tab_title_profile),
        )
    }

    private val mIcons: ArrayList<Int> = arrayListOf(
        R.drawable.tab_ic_tab1,
        R.drawable.tab_ic_tab2,
        R.drawable.tab_ic_tab4,
        R.drawable.tab_ic_tab3,
    )

    private val mTabs: ArrayList<RadioButton> = arrayListOf()

    private val mFragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment(),
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initFragment()
        initTab()
        switchTab(0)
    }

    private fun initFragment() {
        mBinding.viewPager.offscreenPageLimit = 4
        mBinding.viewPager.adapter =
            ViewPagerFragmentAdapter(supportFragmentManager, mFragments)
    }

    private fun initTab() {
        mBinding.navigate.setOnCheckedChangeListener(this)
        for (i in 0 until mBinding.navigate.childCount) {
            val radioButton: RadioButton = mBinding.navigate.getChildAt(i) as RadioButton
            radioButton.text = mTitles[i]
            radioButton.setTextColor(
                ContextCompat.getColorStateList(
                    this,
                    R.color.tab_selector_check_primary_dark
                )
            )
            val drawable = ContextCompat.getDrawable(this, mIcons[i])?.mutate()
            drawable?.let {
                DrawableCompat.setTintList(
                    it,
                    ContextCompat.getColorStateList(this, R.color.tab_selector_check_primary_dark)
                )
                radioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, it, null, null)
            }
            mTabs.add(radioButton)
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        for (i in 0 until mBinding.navigate.childCount) {
            if (mBinding.navigate.getChildAt(i).id == checkedId) {
                switchTab(i)
                switchFragment(i)
            }
        }
    }

    private fun switchTab(position: Int) {
        mBinding.navigate.check(mTabs[position].id)
    }

    private fun switchFragment(position: Int) {
        mBinding.viewPager.currentItem = position
    }
}
