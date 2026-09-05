package com.example.william.my.module.anim.activity

import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityTransitionSecondBinding

/**
 * Activity 过渡动画 — 目标页（Enter Transition）
 *
 * 接收 TransitionFirstActivity 传来的过渡类型，设置对应的 window.enterTransition。
 * Android 5.0（API 21）引入 Transition 框架，支持 Activity 切换时的共享元素和场景过渡动画。
 *
 * 核心原理：
 * 1. 通过 Intent Extra 接收过渡类型（"explode" / "slide" / "fade"）
 * 2. 在 initView 中设置 window.enterTransition（进入动画）
 * 3. BaseActivity.setContentView() 已自动请求 FEATURE_CONTENT_TRANSITIONS
 * 4. finishAfterTransition() 触发返回时的反向过渡动画
 *
 * 三种过渡类型：
 * - Explode：爆炸效果，元素从中心向四周散开
 * - Slide：滑动效果，元素从屏幕边缘滑入
 * - Fade：淡入淡出效果，元素渐显 / 渐隐
 *
 * 适用场景：
 * - Activity 切换时的流畅过渡动画
 * - 替代传统的 overridePendingTransition()
 * - 配合共享元素过渡（Shared Element Transition）使用
 */
@Route(path = RouterPath.Anim.Transition2)
class TransitionSecondActivity : BaseVBActivity<AnimActivityTransitionSecondBinding>() {

    override fun getViewBinding(): AnimActivityTransitionSecondBinding = AnimActivityTransitionSecondBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val transition = intent.getStringExtra("transition")
        when (transition) {
            "explode" -> {
                window.enterTransition = Explode().apply { duration = 1000 }
            }

            "slide" -> {
                window.enterTransition = Slide().apply { duration = 1000 }
            }

            "fade" -> {
                window.enterTransition = Fade().apply { duration = 1000 }
            }
        }

        mBinding.transitionShare.setOnClickListener {
            finishAfterTransition()
        }
    }
}
