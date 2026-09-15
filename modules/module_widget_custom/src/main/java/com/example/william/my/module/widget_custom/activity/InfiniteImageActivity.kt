package com.example.william.my.module.widget_custom.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.DemoActivityInfiniteImageBinding

/**
 * InfiniteImage — 无限轮播图片控件
 *
 * 无限轮播图片控件，支持自动轮播和手动滑动。
 *
 * 核心机制与避坑点：
 * 1. 无限轮播：支持无限循环轮播，无边界感
 * 2. 自动播放：支持自动轮播，可设置间隔时间
 * 3. 手动滑动：支持手势滑动切换
 * 4. 指示器：支持自定义指示器样式
 */
@Route(path = RouterPath.WidgetCustom.InfiniteImage)
class InfiniteImageActivity : BaseVBActivity<DemoActivityInfiniteImageBinding>() {

    override fun getViewBinding(): DemoActivityInfiniteImageBinding = DemoActivityInfiniteImageBinding.inflate(layoutInflater)
}
