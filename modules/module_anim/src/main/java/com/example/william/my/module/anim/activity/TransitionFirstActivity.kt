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
 * 演示 Activity 之间的窗口过渡效果（Window Transitions），提升用户体验。
 *
 * 四种过渡方式：
 * 1. Explode：元素从屏幕四周爆炸散开
 * 2. Slide：内容从边缘滑入
 * 3. Fade：内容淡入
 * 4. Shared Element：共享元素过渡（View 到 View 的连续动画）
 *
 * 核心特性：
 * 1. 平滑过渡：Activity 切换时提供平滑的视觉过渡
 * 2. 共享元素：支持两个 Activity 之间共享元素的连续动画
 * 3. 自定义过渡：可自定义过渡效果和时长
 * 4. 兼容性好：支持 Android 5.0+（API 21+）
 *
 * 基本用法：
 * ```kotlin
 * // 普通过渡
 * val options = ActivityOptions.makeSceneTransitionAnimation(this)
 * startActivity(intent, options.toBundle())
 *
 * // 共享元素过渡
 * val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
 *     this,
 *     Pair(sharedView, "transitionName")
 * )
 * startActivity(intent, options.toBundle())
 * ```
 *
 * 适用场景：
 * - Activity 切换动画
 * - 页面跳转过渡效果
 * - 共享元素连续动画
 */
@Route(path = RouterPath.Anim.Transition)
class TransitionFirstActivity :
    BaseVBActivity<AnimActivityTransitionFirstBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AnimActivityTransitionFirstBinding = AnimActivityTransitionFirstBinding.inflate(layoutInflater)

    private lateinit var mIntent: Intent

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mIntent = Intent(this, TransitionSecondActivity::class.java)

        mBinding.transitionExplode.setOnClickListener(this)
        mBinding.transitionSlide.setOnClickListener(this)
        mBinding.transitionFade.setOnClickListener(this)
        mBinding.transitionShare.setOnClickListener(this)
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
                mIntent.putExtra("transition", type)
                startActivity(
                    mIntent,
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
                            Pair(mBinding.transitionShare, "shareTransition"),
                        ),
                    )
                    .navigation(this)
            }
        }
    }
}
