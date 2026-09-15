package com.example.william.my.module.anim.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * ValueAnimator — 差值动画 + ViewPropertyAnimator
 *
 * ValueAnimator 是属性动画的基础类，按时间产生值，由开发者手动更新 View。
 *
 * 核心机制与避坑点：
 * 1. 时间驱动：按时间产生值，不直接操作 View
 * 2. 精细控制：通过 addUpdateListener 精细控制动画过程
 * 3. 丰富的插值器：支持多种插值器控制动画速度曲线
 * 4. 链式调用：ViewPropertyAnimator 提供更简洁的写法
 *
 * 插值器类型：
 * 1. LinearInterpolator：线性插值器，匀速运动
 * 2. AccelerateInterpolator：加速插值器，越来越快
 * 3. DecelerateInterpolator：减速插值器，越来越慢
 * 4. AccelerateDecelerateInterpolator：先加速后减速，最常用
 * 5. BounceInterpolator：弹跳插值器，模拟弹跳效果
 * 6. OvershootInterpolator：过冲插值器，超出目标后回弹
 * 7. AnticipateInterpolator：回拉插值器，先回拉再前进
 *
 * https://developer.android.google.cn/develop/ui/views/animations/prop-animation
 */
@Route(path = RouterPath.Anim.ValueAnimator)
class ValueAnimatorActivity : BasicImageActivity() {

    private var isAnimating = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.shared_color_primary))
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "LinearInterpolator（线性插值器）",
        "AccelerateInterpolator（加速插值器）",
        "DecelerateInterpolator（减速插值器）",
        "AccelerateDecelerateInterpolator（先加速后减速）",
        "BounceInterpolator（弹跳插值器）",
        "OvershootInterpolator（过冲插值器）",
        "AnticipateInterpolator（回拉插值器）",
        "ViewPropertyAnimator（链式调用）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        if (isAnimating) return
        when (position) {
            0 -> startWithInterpolator(LinearInterpolator())
            1 -> startWithInterpolator(AccelerateInterpolator())
            2 -> startWithInterpolator(DecelerateInterpolator())
            3 -> startWithInterpolator(AccelerateDecelerateInterpolator())
            4 -> startWithInterpolator(BounceInterpolator())
            5 -> startWithInterpolator(OvershootInterpolator())
            6 -> startWithInterpolator(AnticipateInterpolator())
            7 -> startViewPropertyAnimator()
        }
    }

    /**
     * ValueAnimator + 自定义插值器。
     *
     * addUpdateListener 在每一帧回调，可获取当前插值后的值。
     */
    private fun startWithInterpolator(interpolator: Interpolator) {
        isAnimating = true
        binding.basicsImage.rotation = 0f
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            this.interpolator = interpolator
            addUpdateListener { animation ->
                binding.basicsImage.rotation = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                }
            })
            start()
        }
    }

    /**
     * ViewPropertyAnimator 链式调用。
     *
     * 比 ObjectAnimator 更简洁，直接在 View 上调用 animate()，适合简单多属性组合。
     */
    private fun startViewPropertyAnimator() {
        isAnimating = true
        binding.basicsImage.animate()
            .translationX(-400f)
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(1500)
            .withEndAction {
                // 动画结束后恢复初始状态
                binding.basicsImage.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1500)
                    .withEndAction { isAnimating = false }
                    .start()
            }
            .start()
    }
}
