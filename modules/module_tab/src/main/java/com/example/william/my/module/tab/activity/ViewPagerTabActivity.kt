package com.example.william.my.module.tab.activity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityViewPagerTabBinding

/**
 * ViewPager + Tab — ViewPager 实现 Tab 切换
 *
 * 使用 ViewPager + RadioGroup 实现 Tab 切换，支持 Fragment 页面切换。
 *
 * 核心机制与避坑点：
 * 1. ViewPager 联动：Tab 和 ViewPager 联动切换
 * 2. Fragment 支持：支持 Fragment 页面切换
 * 3. 自定义样式：支持自定义 Tab 样式
 * 4. 图标支持：支持图标和文字组合
 *
 * 官方参考：
 * https://developer.android.com/reference/androidx/viewpager/widget/ViewPager
 */
class ViewPagerTabActivity :
    BaseVBActivity<TabActivityViewPagerTabBinding>(),
    RadioGroup.OnCheckedChangeListener {

    override fun getViewBinding(): TabActivityViewPagerTabBinding = TabActivityViewPagerTabBinding.inflate(layoutInflater)

    private val titles: ArrayList<String> by lazy {
        arrayListOf(
            getString(R.string.tab_title_home),
            getString(R.string.tab_title_discover),
            getString(R.string.tab_title_message),
            getString(R.string.tab_title_profile),
        )
    }

    private val icons: ArrayList<Int> = arrayListOf(
        R.drawable.tab_ic_tab1,
        R.drawable.tab_ic_tab2,
        R.drawable.tab_ic_tab4,
        R.drawable.tab_ic_tab3,
    )

    private val tabs: ArrayList<RadioButton> = arrayListOf()

    private val fragments: ArrayList<Fragment> = arrayListOf(
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

    /**
     * 初始化页面 Fragment 与 ViewPager 适配器，并注册页面滑动监听
     */
    private fun initFragment() {
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter =
            ViewPagerFragmentAdapter(supportFragmentManager, fragments)
    }

    /**
     * 绑定底部 Tab 点击监听并初始化默认选中状态
     */
    private fun initTab() {
        binding.navigate.setOnCheckedChangeListener(this)
        for (i in 0 until binding.navigate.childCount) {
            val radioButton: RadioButton = binding.navigate.getChildAt(i) as RadioButton
            radioButton.text = titles[i]
            radioButton.setTextColor(
                ContextCompat.getColorStateList(
                    this,
                    R.color.tab_selector_check_primary_dark,
                ),
            )
            val drawable = ContextCompat.getDrawable(this, icons[i])?.mutate()
            drawable?.let {
                DrawableCompat.setTintList(
                    it,
                    ContextCompat.getColorStateList(this, R.color.tab_selector_check_primary_dark),
                )
                radioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, it, null, null)
            }
            tabs.add(radioButton)
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        for (i in 0 until binding.navigate.childCount) {
            val child = binding.navigate.getChildAt(i) as? RadioButton
            val isChecked = child?.id == checkedId
            child?.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (isChecked) {
                switchTab(i)
                switchFragment(i)
            }
        }
    }

    /**
     * 切换底部 Tab 图标与文字选中态，并更新当前索引
     */
    private fun switchTab(position: Int) {
        binding.navigate.check(tabs[position].id)
    }

    /**
     * 切换 ViewPager 当前页并同步更新底部 Tab 状态
     */
    private fun switchFragment(position: Int) {
        binding.viewPager.currentItem = position
    }
}
