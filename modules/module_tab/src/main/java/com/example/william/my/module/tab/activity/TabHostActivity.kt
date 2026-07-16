package com.example.william.my.module.tab.activity

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
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityTabHostBinding

@Route(path = RouterPath.Tab.TabHost)
class TabHostActivity : BaseVBActivity<TabActivityTabHostBinding>() {

    override fun getViewBinding(): TabActivityTabHostBinding {
        return TabActivityTabHostBinding.inflate(layoutInflater)
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

    private val mFragments: ArrayList<Class<*>> = arrayListOf(
        PrimaryFragment::class.java,
        PrimaryDarkFragment::class.java,
        PrimaryFragment::class.java,
        PrimaryDarkFragment::class.java
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initTab()
    }

    private fun initTab() {
        mBinding.tabhost.setup(this, supportFragmentManager, android.R.id.tabcontent)
        mBinding.tabhost.setOnTabChangedListener {
            switchTab(it)
        }

        for (i in mFragments.indices) {
            val tabView = getTabView(i)
            val tabSpec = mBinding.tabhost.newTabSpec(mTitles[i]).setIndicator(tabView)
            mBinding.tabhost.addTab(tabSpec, mFragments[i], null)
        }
    }

    private fun getTabView(position: Int): View {
        val view = layoutInflater.inflate(R.layout.tab_item_tab_host, mBinding.tabs, false)
        val textView = view.findViewById<TextView>(R.id.item_tab_text)
        textView.text = mTitles[position]
        textView.setTextColor(
            ContextCompat.getColorStateList(
                this,
                R.color.tab_selector_select_primary_dark
            )
        )
        val drawable = ContextCompat.getDrawable(this, mIcons[position])?.mutate()
        drawable?.let {
            DrawableCompat.setTintList(
                it,
                ContextCompat.getColorStateList(this, R.color.tab_selector_select_primary_dark)
            )
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, it, null, null)
        }
        return view
    }

    private fun switchTab(tabTag: String) {
        Utils.toast(tabTag)
    }
}
