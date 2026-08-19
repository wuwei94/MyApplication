package com.example.william.my.module.widget.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget.databinding.DemoActivityInfiniteImageBinding

/**
 * InfiniteImage — 无限轮播图片控件
 *
 * 无限轮播图片控件，支持自动轮播和手动滑动。
 *
 * 核心特性：
 * 1. 无限轮播：支持无限循环轮播，无边界感
 * 2. 自动播放：支持自动轮播，可设置间隔时间
 * 3. 手动滑动：支持手势滑动切换
 * 4. 指示器：支持自定义指示器样式
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.example.widget.InfiniteImageView
 *     android:layout_width="match_parent"
 *     android:layout_height="200dp"
 *     app:autoPlay="true"
 *     app:interval="3000" />
 *
 * // 代码中设置数据
 * infiniteImageView.setImages(imageList)
 * infiniteImageView.startAutoPlay()
 * ```
 *
 * 适用场景：
 * - 首页轮播广告
 * - 商品展示轮播
 * - 引导页、欢迎页
 */
@Route(path = RouterPath.Widget.InfiniteImage)
class InfiniteImageActivity : BaseVBActivity<DemoActivityInfiniteImageBinding>() {

    override fun getViewBinding(): DemoActivityInfiniteImageBinding {
        return DemoActivityInfiniteImageBinding.inflate(layoutInflater)
    }
}