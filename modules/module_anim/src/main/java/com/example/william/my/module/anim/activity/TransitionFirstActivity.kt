package com.example.william.my.module.anim.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.R
import com.example.william.my.module.anim.databinding.AnimActivityTransitionFirstBinding

/**
 * Activity 跳转过渡动画 — 窗口过渡效果
 *
 * 演示 Activity 之间的窗口过渡（Window Transitions），目标页为 [TransitionSecondActivity]。
 *
 * 核心机制与避坑点：
 * 1. Explode：元素从屏幕四周爆炸散开
 * 2. Slide：内容从边缘滑入
 * 3. Fade：内容淡入
 * 4. Shared Element：共享元素过渡（View 到 View 的连续动画）
 *
 * https://developer.android.google.cn/develop/ui/views/animations/transitions
 */
@Route(path = RouterPath.Anim.Transition)
class TransitionFirstActivity :
    BaseVBActivity<AnimActivityTransitionFirstBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AnimActivityTransitionFirstBinding = AnimActivityTransitionFirstBinding.inflate(layoutInflater)

    private lateinit var transitionIntent: Intent

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        transitionIntent = Intent(this, TransitionSecondActivity::class.java)

        binding.transitionExplode.setOnClickListener(this)
        binding.transitionSlide.setOnClickListener(this)
        binding.transitionFade.setOnClickListener(this)
        binding.transitionShare.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.transition_explode,
            R.id.transition_slide,
            R.id.transition_fade,
            -> {
                val type = when (v.id) {
                    R.id.transition_explode -> "explode"
                    R.id.transition_slide -> "slide"
                    else -> "fade"
                }
                transitionIntent.putExtra("transition", type)
                startActivity(
                    transitionIntent,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle(),
                )
            }

            R.id.transition_share -> {
                ARouter.getInstance()
                    .build(RouterPath.Anim.Transition2)
                    .withString("transition", "share")
                    .withOptionsCompat(
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            Pair(binding.transitionShare, "shareTransition"),
                        ),
                    )
                    .navigation(this)
            }
        }
    }
}
