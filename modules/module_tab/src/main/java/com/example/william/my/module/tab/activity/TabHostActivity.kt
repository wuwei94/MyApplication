package com.example.william.my.module.tab.activity

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityTabHostBinding

/**
 * TabHost — 传统 Tab 切换（已废弃）
 *
 * TabHost 是 Android 传统的 Tab 切换控件，已废弃，推荐使用 TabLayout + ViewPager。
 *
 * 核心特性：
 * 1. 传统 Tab：支持 Tab 切换和内容切换
 * 2. 自定义 Tab：支持自定义 Tab 样式
 * 3. Fragment 支持：支持 Fragment 切换
 *
 * 基本用法：
 * ```kotlin
 * // 初始化 TabHost
 * tabHost.setup(context, fragmentManager, containerId)
 *
 * // 添加 Tab
 * val tabSpec = tabHost.newTabSpec("tag")
 *     .setIndicator(tabView)
 * tabHost.addTab(tabSpec, Fragment::class.java, null)
 * ```
 *
 * 注意事项：
 * - TabHost 已废弃，推荐使用 TabLayout + ViewPager
 * - 仅用于学习和兼容旧代码
 *
 * 适用场景：
 * - 兼容旧代码
 * - 学习传统 Tab 实现
 */
@Route(path = RouterPath.Tab.TabHost)
class TabHostActivity : BaseVBActivity<TabActivityTabHostBinding>() {

    override fun getViewBinding(): TabActivityTabHostBinding = TabActivityTabHostBinding.inflate(layoutInflater)

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

    private val mFragments: ArrayList<Class<*>> = arrayListOf(
        PrimaryFragment::class.java,
        PrimaryDarkFragment::class.java,
        PrimaryFragment::class.java,
        PrimaryDarkFragment::class.java,
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initTab()
    }

    private fun initTab() {
        mBinding.tabhost.setup(this, supportFragmentManager, android.R.id.tabcontent)
        mBinding.tabhost.setOnTabChangedListener {
            updateTabTextBold(it)
            switchTab(it)
        }

        for (i in mFragments.indices) {
            val tabView = getTabView(i)
            val tabSpec = mBinding.tabhost.newTabSpec(mTitles[i]).setIndicator(tabView)
            mBinding.tabhost.addTab(tabSpec, mFragments[i], null)
        }
        updateTabTextBold(mTitles[0])
    }

    private fun updateTabTextBold(selectedTag: String) {
        for (i in mFragments.indices) {
            val tabView = mBinding.tabs.getChildTabViewAt(i)
            val textView = tabView?.findViewById<TextView>(R.id.item_tab_text)
            val isSelected = mTitles[i] == selectedTag
            textView?.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    private fun getTabView(position: Int): View {
        val view = layoutInflater.inflate(R.layout.tab_item_tab_host, mBinding.tabs, false)
        val textView = view.findViewById<TextView>(R.id.item_tab_text)
        textView.text = mTitles[position]
        textView.setTextColor(
            ContextCompat.getColorStateList(
                this,
                R.color.tab_selector_select_primary_dark,
            ),
        )
        val drawable = ContextCompat.getDrawable(this, mIcons[position])?.mutate()
        drawable?.let {
            DrawableCompat.setTintList(
                it,
                ContextCompat.getColorStateList(this, R.color.tab_selector_select_primary_dark),
            )
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, it, null, null)
        }
        return view
    }

    private fun switchTab(tabTag: String) {
        Utils.toast(tabTag)
    }
}
