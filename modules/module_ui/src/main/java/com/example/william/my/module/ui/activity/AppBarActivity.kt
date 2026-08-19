package com.example.william.my.module.ui.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.ui.databinding.UiActivityAppBarBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

/**
 * AppBar — Material Design 应用栏
 *
 * 使用 AppBarLayout + CollapsingToolbarLayout 实现 Material Design 应用栏。
 *
 * 核心特性：
 * 1. 折叠效果：支持 CollapsingToolbarLayout 折叠效果
 * 2. 滚动标志：支持多种滚动标志控制滚动行为
 * 3. Tab 支持：支持 TabLayout + ViewPager 联动
 * 4. 自定义样式：支持自定义样式和主题
 *
 * 滚动标志（layout_scrollFlags）：
 * 1. scroll：首先滑动的是列表，列表的数据全部滚动完毕，才开始 toolbar 滑动
 * 2. enterAlways：首先滑动的是 toolbar，然后再去滑动其他的 view
 * 3. enterAlwaysCollapsed：向下滚动事件时，子 View 先向下滚动最小高度值，然后 Scrolling View 开始滚动
 * 4. exitUntilCollapsed：向上滚动事件时，子 View 向上滚动直至最小高度，然后 Scrolling View 开始滚动
 *
 * 折叠模式（layout_collapseMode）：
 * 1. pin：当 CollapsingToolbarLayout 完全收缩后，Toolbar 还可以保留在屏幕上
 * 2. parallax：在内容滚动时，CollapsingToolbarLayout 中的 View 也可以同时滚动，实现视差滚动效果
 *
 * Tab 布局模式（tabMode）：
 * 1. MODE_FIXED：固定均分，所有 Tab 等宽排列，适合少量固定 Tab（3-5 个）
 * 2. MODE_SCROLLABLE：可滚动，Tab 按内容宽度排列，数量多时可左右滑动
 *
 * 适用场景：
 * - Material Design 应用栏
 * - 折叠效果
 * - Tab 切换
 */
@Route(path = RouterPath.UI.Appbar)
class AppBarActivity : BaseVBActivity<UiActivityAppBarBinding>() {

    override fun getViewBinding(): UiActivityAppBarBinding {
        return UiActivityAppBarBinding.inflate(layoutInflater)
    }

    private val mTitles: ArrayList<String> = arrayListOf(
        "primary1",
        "primaryDark1",
        "primary2",
        "primaryDark2",
    )

    private val mFragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment(),
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initAppBar()
    }

    private fun initAppBar() {
        setSupportActionBar(mBinding.toolbar)
        mBinding.toolbarLayout.title = title

        mBinding.viewPager.adapter =
            ViewPagerFragmentAdapter(supportFragmentManager, mFragments, mTitles)

        //设置TabLayout可滚动，保证Tab数量过多时也可正常显示
        mBinding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        //设置TabLayout选中Tab下划线颜色
        mBinding.tabLayout.setSelectedTabIndicatorColor(
            ContextCompat.getColor(
                this,
                com.example.william.my.basic.basic_shared.R.color.shared_color_primary_dark
            )
        )
        //两个参数分别对应Tab未选中的文字颜色和选中的文字颜色
        mBinding.tabLayout.setTabTextColors(
            ContextCompat.getColor(
                this,
                com.example.william.my.basic.basic_shared.R.color.shared_color_primary
            ),
            ContextCompat.getColor(
                this,
                com.example.william.my.basic.basic_shared.R.color.shared_color_primary_dark
            )
        )
        //绑定ViewPager
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager)

        //设置TabLayout的选择监听
        mBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mBinding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            /*
             * 重复点击Tab时回调
             */
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}
