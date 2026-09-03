package com.example.william.my.module.widget.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.adapter.ViewPagerAdapter
import com.example.william.my.basic.basic_shared.adapter.ViewPagerFragmentAdapter
import com.example.william.my.basic.basic_shared.fragment.PrimaryDarkFragment
import com.example.william.my.basic.basic_shared.fragment.PrimaryFragment
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget.databinding.UiActivityViewPagerBinding

/**
 * ViewPager — 页面滑动控件
 *
 * ViewPager 是 Android 原生的页面滑动控件，支持左右滑动切换页面。
 *
 * 核心特性：
 * 1. 页面滑动：支持左右滑动切换页面
 * 2. Fragment 支持：支持 Fragment 页面切换
 * 3. 适配器模式：使用 PagerAdapter 或 FragmentPagerAdapter
 * 4. 缓存机制：支持页面缓存，提升性能
 *
 * 基本用法：
 * ```kotlin
 * // 设置适配器
 * viewPager.adapter = ViewPagerAdapter(titles)
 *
 * // Fragment 适配器
 * viewPager.adapter = ViewPagerFragmentAdapter(supportFragmentManager, fragments)
 * ```
 *
 * 适用场景：
 * - 引导页、欢迎页
 * - Tab 切换
 * - 图片轮播
 */
@Route(path = RouterPath.Widget.ViewPager)
class ViewPagerActivity : BaseVBActivity<UiActivityViewPagerBinding>() {

    override fun getViewBinding(): UiActivityViewPagerBinding {
        return UiActivityViewPagerBinding.inflate(layoutInflater)
    }

    private val mTitles: ArrayList<String> = arrayListOf(
        "primary1",
        "primaryDark1",
        "primary2",
        "primaryDark2"
    )

    private val mFragments: ArrayList<Fragment> = arrayListOf(
        PrimaryFragment(),
        PrimaryDarkFragment(),
        PrimaryFragment(),
        PrimaryDarkFragment()
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initViewPager()
    }

    private fun initViewPager() {
        mBinding.viewpagerView.adapter = ViewPagerAdapter(mTitles)

        mBinding.viewpagerFragment.adapter = ViewPagerFragmentAdapter(
            supportFragmentManager, mFragments
        )
    }
}