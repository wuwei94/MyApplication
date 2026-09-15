package com.example.william.my.module.anim.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityLottieBinding

/**
 * Lottie — 高性能动画渲染框架
 *
 * Lottie 是 Airbnb 开源的动画库，可直接渲染 Adobe After Effects 导出的动画。
 *
 * 核心机制与避坑点：
 * 1. 矢量动画：支持矢量图形，无失真缩放
 * 2. 文件体积小：JSON 格式，比 GIF 小 10 倍以上
 * 3. 高性能：硬件加速渲染，流畅运行复杂动画
 * 4. 动态控制：支持播放、暂停、进度控制、速度调整
 *
 * https://github.com/airbnb/lottie-android
 */
@Route(path = RouterPath.Anim.Lottie)
class LottieActivity : BaseVBActivity<AnimActivityLottieBinding>() {

    override fun getViewBinding(): AnimActivityLottieBinding = AnimActivityLottieBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 布局已配置 lottie_autoPlay；需要结束/取消回调时再挂 AnimatorListener
    }
}
