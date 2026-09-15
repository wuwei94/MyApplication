package com.example.william.my.module.tab.activity

import android.graphics.Typeface
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
 * https://developer.android.com/reference/androidx/viewpager/widget/ViewPager
 */
@Route(path = RouterPath.Tab.ViewPagerTab)
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

    private fun initFragment() {
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter =
            ViewPagerFragmentAdapter(supportFragmentManager, fragments)
    }

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

    private fun switchTab(position: Int) {
        binding.navigate.check(tabs[position].id)
    }

    private fun switchFragment(position: Int) {
        binding.viewPager.currentItem = position
    }
}
