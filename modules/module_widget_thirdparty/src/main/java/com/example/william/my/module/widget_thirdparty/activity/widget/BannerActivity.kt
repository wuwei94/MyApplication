package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityBannerBinding
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator

/**
 * Banner — 轮播图组件
 *
 * Banner 是一个强大的轮播图库，支持多种指示器样式和自定义适配器。
 *
 * 核心特性：
 * 1. 多种指示器：圆形、数字、自定义指示器
 * 2. 自动轮播：支持设置轮播间隔时间
 * 3. 生命周期感知：自动在 onPause 停止，onResume 恢复
 * 4. 丰富的自定义：支持自定义页面切换动画、指示器样式
 *
 * 基本用法：
 * ```kotlin
 * banner.setAdapter(adapter)
 *     .setIndicator(CircleIndicator(context))
 *     .setLoopTime(3000)  // 轮播间隔
 *     .addBannerLifecycleObserver(this)  // 绑定生命周期
 * ```
 *
 * 适用场景：
 * - 首页轮播广告
 * - 商品展示轮播
 * - 引导页、欢迎页
 *
 * https://github.com/youth5201314/banner
 */
@Route(path = RouterPath.WidgetThirdparty.Banner)
class BannerActivity : BaseVBActivity<WidgetThirdpartyActivityBannerBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityBannerBinding {
        return WidgetThirdpartyActivityBannerBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBanner()
    }

    private fun initBanner() {

        mBinding.banner.setAdapter(object :
            BannerImageAdapter<String>(arrayListOf("1", "2", "3", "4")) {
            override fun onBindView(
                holder: BannerImageHolder, data: String, position: Int, size: Int
            ) {
                holder.imageView?.setImageResource(R.drawable.shared_ic_launcher)
            }
        })
            .setIndicator(CircleIndicator(this))
            .addBannerLifecycleObserver(this) // 添加生命周期观察者
    }
}
