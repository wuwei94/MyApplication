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
 * 接收 [TransitionFirstActivity] 传来的过渡类型并设置 `window.enterTransition`。
 *
 * 核心机制与避坑点：
 * 1. Intent Extra 驱动：按 "explode" / "slide" / "fade" 选择进入动画
 * 2. Enter Transition：在 `initView` 中设置目标页窗口进入效果
 * 3. 反向过渡：`finishAfterTransition()` 触发返回时的反向动画
 * 4. 宿主已就绪：BaseActivity 已请求 `FEATURE_CONTENT_TRANSITIONS`
 *
 * 入口页与四种过渡方式见 [TransitionFirstActivity]。
 *
 * https://developer.android.google.cn/develop/ui/views/animations/transitions
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

        binding.transitionShare.setOnClickListener {
            finishAfterTransition()
        }
    }
}
