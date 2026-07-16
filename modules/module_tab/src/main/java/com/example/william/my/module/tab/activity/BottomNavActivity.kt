package com.example.william.my.module.tab.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityBottomNavBinding
import com.example.william.my.module.tab.utils.FragmentUtils

@Route(path = RouterPath.Tab.BottomNav)
class BottomNavActivity : BaseVBActivity<TabActivityBottomNavBinding>() {

    override fun getViewBinding(): TabActivityBottomNavBinding {
        return TabActivityBottomNavBinding.inflate(layoutInflater)
    }

    private val mTitles by lazy {
        arrayListOf(
            getString(R.string.tab_title_home),
            getString(R.string.tab_title_discover),
            getString(R.string.tab_title_message),
            getString(R.string.tab_title_profile),
        )
    }

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
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        FragmentUtils.initFragment(
            savedInstanceState, supportFragmentManager,
            R.id.frameLayout, mFragments, mTitles
        )
    }

    private fun initTab() {
        mBinding.bottomNav.setOnItemSelectedListener { item ->
            val index = when (item.itemId) {
                R.id.nav_tab1 -> 0
                R.id.nav_tab2 -> 1
                R.id.nav_tab3 -> 2
                R.id.nav_tab4 -> 3
                else -> 0
            }
            switchFragment(index)
            true
        }
    }

    private fun switchFragment(position: Int) {
        FragmentUtils.switchFragment(supportFragmentManager, mFragments, position)
    }
}
