package com.example.william.my.module.tab.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.tab.databinding.TabActivityFlycoTabLayoutBinding
import com.example.william.my.module.tab.utils.TabLayoutUtils

/**
 * FlycoTabLayout — 强大的 TabLayout 库
 *
 * FlycoTabLayout 是一个功能丰富的 TabLayout 库，提供多种 Tab 样式。
 *
 * 核心机制与避坑点：
 * 1. 多种样式：SlidingTab、CommonTab、SegmentTab 三种样式
 * 2. 丰富的自定义：支持自定义指示器、角标、图标
 * 3. 与 ViewPager 联动：自动与 ViewPager 同步
 * 4. 动态更新：支持动态添加、删除 Tab
 *
 * https://github.com/H07000223/FlycoTabLayout
 */
@Route(path = RouterPath.Tab.FlycoTabLayout)
class FlycoTabLayoutActivity : BaseVBActivity<TabActivityFlycoTabLayoutBinding>() {

    override fun getViewBinding(): TabActivityFlycoTabLayoutBinding = TabActivityFlycoTabLayoutBinding.inflate(layoutInflater)

    private var titles: ArrayList<String> = arrayListOf()

    private val fragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment(),
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        intTitles()
        initTabLayout()
    }

    private fun intTitles() {
        titles = arrayListOf("primary1", "primaryDark1", "primary2", "primaryDark2")
    }

    private fun initTabLayout() {
        TabLayoutUtils.initSlidingTab(
            binding.slidingTab,
            binding.viewPager,
            titles,
            this,
            fragments,
        )
        TabLayoutUtils.initCommonTabLayout(
            binding.commonTab,
            binding.viewPager,
            titles,
        )

        TabLayoutUtils.initSegmentTabLayout(
            binding.segmentTab,
            binding.viewPager,
            titles,
        )

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.commonTab.currentTab = position
                binding.segmentTab.currentTab = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }
}
