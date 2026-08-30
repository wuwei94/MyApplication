package com.example.william.my.module.tab.activity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityFrameLayoutTabBinding
import com.example.william.my.module.tab.utils.FragmentUtils

/**
 * FrameLayoutTab — RadioGroup + FrameLayout 实现 Tab 切换
 *
 * 使用 RadioGroup + RadioButton + FrameLayout 实现 Tab 切换，适合简单场景。
 *
 * 核心特性：
 * 1. 简单实现：使用 RadioGroup 实现 Tab 切换
 * 2. Fragment 切换：支持 Fragment 切换
 * 3. 自定义样式：支持自定义 Tab 样式
 * 4. 选中状态：支持选中/未选中状态切换
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <RadioGroup
 *     android:id="@+id/navigate"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:orientation="horizontal">
 *     <RadioButton ... />
 *     <RadioButton ... />
 * </RadioGroup>
 *
 * // 代码中设置监听
 * radioGroup.setOnCheckedChangeListener { group, checkedId ->
 *     // 切换 Fragment
 * }
 * ```
 *
 * 适用场景：
 * - 简单的 Tab 切换
 * - 底部导航栏
 * - 不需要复杂动画的场景
 */
@Route(path = RouterPath.Tab.FrameLayoutTab)
class FrameLayoutTabActivity : BaseVBActivity<TabActivityFrameLayoutTabBinding>(),
    RadioGroup.OnCheckedChangeListener {

    override fun getViewBinding(): TabActivityFrameLayoutTabBinding {
        return TabActivityFrameLayoutTabBinding.inflate(layoutInflater)
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

        initFragment(savedInstanceState)
        initTab()
        switchTab(0)
        switchFragment(0)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        FragmentUtils.initFragment(
            savedInstanceState, supportFragmentManager,
            R.id.frameLayout, mFragments, mTitles
        )
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
        FragmentUtils.switchFragment(supportFragmentManager, mFragments, position)
    }
}
