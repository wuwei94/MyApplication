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
 * 核心特性：
 * 1. 多种样式：SlidingTab、CommonTab、SegmentTab 三种样式
 * 2. 丰富的自定义：支持自定义指示器、角标、图标
 * 3. 与 ViewPager 联动：自动与 ViewPager 同步
 * 4. 动态更新：支持动态添加、删除 Tab
 *
 * 基本用法：
 * ```kotlin
 * // SlidingTabLayout
 * slidingTabLayout.setViewPager(viewPager, titles)
 *
 * // CommonTabLayout
 * commonTabLayout.setTabData(tabEntities)
 *
 * // SegmentTabLayout
 * segmentTabLayout.setTabData(titles)
 * ```
 *
 * 适用场景：
 * - 首页底部导航、顶部导航
 * - 分类筛选、标签切换
 * - 多 Tab 页面切换
 *
 * https://github.com/H07000223/FlycoTabLayout
 */
@Route(path = RouterPath.Tab.FlycoTabLayout)
class FlycoTabLayoutActivity : BaseVBActivity<TabActivityFlycoTabLayoutBinding>() {

    override fun getViewBinding(): TabActivityFlycoTabLayoutBinding {
        return TabActivityFlycoTabLayoutBinding.inflate(layoutInflater)
    }

    private var mTitles: ArrayList<String> = arrayListOf()

    private val mFragments: ArrayList<Fragment> = arrayListOf(
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
        mTitles = arrayListOf("primary1", "primaryDark1", "primary2", "primaryDark2")
    }

    private fun initTabLayout() {
        TabLayoutUtils.initSlidingTab(
            mBinding.slidingTab, mBinding.viewPager,
            mTitles, this, mFragments
        )
        TabLayoutUtils.initCommonTabLayout(
            mBinding.commonTab, mBinding.viewPager,
            mTitles
        )

        TabLayoutUtils.initSegmentTabLayout(
            mBinding.segmentTab, mBinding.viewPager,
            mTitles
        )

        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                mBinding.commonTab.currentTab = position
                mBinding.segmentTab.currentTab = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }
}
