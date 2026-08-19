package com.example.william.my.module.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.basic.basic_shared.adapter.ViewPagerAdapter2
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter2
import com.example.william.my.module.ui.databinding.UiActivityViewPager2Binding

/**
 * ViewPager2 — 现代化页面滑动控件
 *
 * ViewPager2 是 AndroidX 提供的现代化页面滑动控件，替代 ViewPager。
 *
 * 核心特性：
 * 1. 基于 RecyclerView：性能更好，支持更多布局
 * 2. 垂直滑动：支持垂直方向滑动
 * 3. Fragment 支持：支持 Fragment 页面切换
 * 4. 适配器模式：使用 RecyclerView.Adapter
 *
 * 注意事项：
 * - ViewPager2 内的 view 布局必须是 match_parent，否则会报错
 *
 * 基本用法：
 * ```kotlin
 * // 设置适配器
 * viewPager2.adapter = ViewPagerAdapter2(titles)
 *
 * // 设置方向
 * viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
 *
 * // Fragment 适配器
 * viewPager2.adapter = ViewPagerFragmentAdapter2(supportFragmentManager, lifecycle, fragments)
 * ```
 *
 * 适用场景：
 * - 引导页、欢迎页
 * - Tab 切换
 * - 图片轮播
 * - 需要垂直滑动的场景
 *
 * https://developer.android.google.cn/jetpack/androidx/releases/viewpager2
 */
@Route(path = RouterPath.UI.ViewPager2)
class ViewPager2Activity : BaseVBActivity<UiActivityViewPager2Binding>() {

    override fun getViewBinding(): UiActivityViewPager2Binding {
        return UiActivityViewPager2Binding.inflate(layoutInflater)
    }

    private val mTitles: ArrayList<String> = arrayListOf(
        "primary1",
        "primary2",
        "primaryDark1",
        "primaryDark2"
    )

    private val mFragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment(),
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initViewPager()
    }

    private fun initViewPager() {
        mBinding.viewpager2View.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding.viewpager2View.adapter = ViewPagerAdapter2(mTitles)

        mBinding.viewpager2Fragment.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding.viewpager2Fragment.adapter = ViewPagerFragmentAdapter2(
            supportFragmentManager, lifecycle, mFragments
        )
    }
}
