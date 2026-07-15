package com.example.william.my.module.anim.activity

import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityTransitionSecondBinding

/**
 * Activity 跳转过渡动画 — 目标页
 *
 * 接收 TransitionFirstActivity 传来的过渡类型，设置 window.enterTransition。
 * BaseActivity.setContentView() 已自动请求 FEATURE_CONTENT_TRANSITIONS，
 * 只需在 initView 中设置过渡即可。
 */
@Route(path = RouterPath.Anim.Transition2)
class TransitionSecondActivity : BaseVBActivity<AnimActivityTransitionSecondBinding>() {

    override fun getViewBinding(): AnimActivityTransitionSecondBinding {
        return AnimActivityTransitionSecondBinding.inflate(layoutInflater)
    }

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
