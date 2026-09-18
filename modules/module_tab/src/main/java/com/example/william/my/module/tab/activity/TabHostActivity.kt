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
 * TabHost 是 Android 早期的 Tab 切换控件，配合 TabSpec 与 Fragment 实现选项卡内容切换。
 *
 * ⚠️ 历史参考：TabHost 已废弃，生产代码应使用 TabLayout + ViewPager2（或 NavigationBar）替代。
 *
 * 核心机制与避坑点：
 * 1. TabSpec 管理：newTabSpec + setIndicator 定义每个 Tab 的标签与视图
 * 2. Fragment 内容：addTab(tabSpec, Fragment::class.java, null) 绑定 Fragment
 * 3. 自定义 Tab 视图：通过 inflate 自定义 indicator 布局实现图标+文字样式
 *
 * https://developer.android.com/reference/android/widget/TabHost
 */
@Route(path = RouterPath.Tab.TabHost)
class TabHostActivity : BaseVBActivity<TabActivityTabHostBinding>() {

    override fun getViewBinding(): TabActivityTabHostBinding = TabActivityTabHostBinding.inflate(layoutInflater)

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

    private val fragments: ArrayList<Class<*>> = arrayListOf(
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
        binding.tabhost.setup(this, supportFragmentManager, android.R.id.tabcontent)
        binding.tabhost.setOnTabChangedListener {
            updateTabTextBold(it)
            switchTab(it)
        }

        for (i in fragments.indices) {
            val tabView = getTabView(i)
            val tabSpec = binding.tabhost.newTabSpec(titles[i]).setIndicator(tabView)
            binding.tabhost.addTab(tabSpec, fragments[i], null)
        }
        updateTabTextBold(titles[0])
    }

    private fun updateTabTextBold(selectedTag: String) {
        for (i in fragments.indices) {
            val tabView = binding.tabs.getChildTabViewAt(i)
            val textView = tabView?.findViewById<TextView>(R.id.item_tab_text)
            val isSelected = titles[i] == selectedTag
            textView?.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    private fun getTabView(position: Int): View {
        val view = layoutInflater.inflate(R.layout.tab_item_tab_host, binding.tabs, false)
        val textView = view.findViewById<TextView>(R.id.item_tab_text)
        textView.text = titles[position]
        textView.setTextColor(
            ContextCompat.getColorStateList(
                this,
                R.color.tab_selector_select_primary_dark,
            ),
        )
        val drawable = ContextCompat.getDrawable(this, icons[position])?.mutate()
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
