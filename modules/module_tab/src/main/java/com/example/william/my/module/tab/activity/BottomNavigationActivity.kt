package com.example.william.my.module.tab.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.tab.R
import com.example.william.my.module.tab.databinding.TabActivityBottomNavigationBinding
import com.example.william.my.module.tab.utils.FragmentUtils

/**
 * BottomNavigation — 底部导航栏
 *
 * 使用 BottomNavigationView 实现底部导航栏，支持 Fragment 切换。
 *
 * 核心机制与避坑点：
 * 1. Material Design：遵循 Material Design 设计规范
 * 2. Fragment 切换：支持 Fragment 切换，保持状态
 * 3. 图标支持：支持图标和文字组合
 * 4. 选中状态：支持选中/未选中状态切换
 *
 * https://material.io/components/bottom-navigation/android
 */
@Route(path = RouterPath.Tab.BottomNavigation)
class BottomNavigationActivity : BaseVBActivity<TabActivityBottomNavigationBinding>() {

    override fun getViewBinding(): TabActivityBottomNavigationBinding = TabActivityBottomNavigationBinding.inflate(layoutInflater)

    private val titles by lazy {
        arrayListOf(
            getString(R.string.tab_title_home),
            getString(R.string.tab_title_discover),
            getString(R.string.tab_title_message),
            getString(R.string.tab_title_profile),
        )
    }

    private val fragments: ArrayList<Fragment> = arrayListOf(
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
            savedInstanceState,
            supportFragmentManager,
            R.id.frameLayout,
            fragments,
            titles,
        )
    }

    private fun initTab() {
        binding.bottomNav.setOnItemSelectedListener { item ->
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
        FragmentUtils.switchFragment(supportFragmentManager, fragments, position)
    }
}
