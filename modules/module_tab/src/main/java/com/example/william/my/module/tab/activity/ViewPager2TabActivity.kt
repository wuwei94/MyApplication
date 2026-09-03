package com.example.william.my.module.tab.activity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter2
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityViewPager2TabBinding

/**
 * ViewPager2 + Tab — ViewPager2 实现 Tab 切换
 *
 * 使用 ViewPager2 + RadioGroup 实现 Tab 切换，支持 Fragment 页面切换。
 *
 * 核心特性：
 * 1. ViewPager2 联动：Tab 和 ViewPager2 联动切换
 * 2. Fragment 支持：支持 Fragment 页面切换
 * 3. 禁用滑动：支持禁用用户手动滑动
 * 4. 页面同步：支持页面变化同步到 Tab
 *
 * 基本用法：
 * ```kotlin
 * // 设置 ViewPager2 适配器
 * viewPager2.adapter = ViewPagerFragmentAdapter2(supportFragmentManager, lifecycle, fragments)
 *
 * // 禁用用户手动滑动
 * viewPager2.isUserInputEnabled = false
 *
 * // 注册页面变化回调
 * viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
 *     override fun onPageSelected(position: Int) {
 *         // 同步 Tab
 *     }
 * })
 * ```
 *
 * 适用场景：
 * - Tab 切换
 * - 底部导航栏
 * - 多 Tab 页面切换
 * - 需要禁用滑动的场景
 */
@Route(path = RouterPath.Tab.ViewPager2Tab)
class ViewPager2TabActivity : BaseVBActivity<TabActivityViewPager2TabBinding>(),
    RadioGroup.OnCheckedChangeListener {

    override fun getViewBinding(): TabActivityViewPager2TabBinding {
        return TabActivityViewPager2TabBinding.inflate(layoutInflater)
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
        mBinding.viewPager2.offscreenPageLimit = 4
        mBinding.viewPager2.adapter =
            ViewPagerFragmentAdapter2(supportFragmentManager, lifecycle, mFragments)

        // 禁止手动滑动，只通过 RadioGroup 切换
        mBinding.viewPager2.isUserInputEnabled = false

        // 同步 ViewPager2 页面变化到 RadioGroup
        mBinding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                switchTab(position)
            }
        })
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
            val child = mBinding.navigate.getChildAt(i) as? RadioButton
            val isChecked = child?.id == checkedId
            child?.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (isChecked) {
                switchTab(i)
                switchFragment(i)
            }
        }
    }

    private fun switchTab(position: Int) {
        mBinding.navigate.check(mTabs[position].id)
    }

    private fun switchFragment(position: Int) {
        mBinding.viewPager2.currentItem = position
    }
}
