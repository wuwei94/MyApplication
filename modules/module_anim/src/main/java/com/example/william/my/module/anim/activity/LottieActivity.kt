package com.example.william.my.module.anim.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityLottieBinding

/**
 * Lottie — JSON 矢量动画布局预览
 *
 * 核心机制与避坑点：
 * 1. JSON 动画：渲染 After Effects 导出的 Lottie JSON，支持缩放无失真
 * 2. 自动播放：布局配置 lottie_autoPlay；暂停/进度控制需在代码中接 LottieAnimationView API
 * 3. 布局预览：本页无交互操作项，复杂控制见官方文档
 *
 * 官方参考：
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
