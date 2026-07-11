package com.example.william.my.module.anim.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.R
import com.example.william.my.basic.basic_module.activity.BasicImageActivity
import com.example.william.my.basic.basic_module.router.path.RouterPath

/**
 * ValueAnimator — 差值动画 + ViewPropertyAnimator
 *
 * ValueAnimator 不直接操作 View，而是按时间产生值，由 addUpdateListener 回调手动更新。
 * 适合需要精细控制动画过程的场景。
 *
 * ViewPropertyAnimator 是更简洁的属性动画写法，适合链式调用。
 */
@Route(path = RouterPath.Anim.ValueAnimator)
class ValueAnimatorActivity : BasicImageActivity() {

    private var index = -1
    private var isAnimating = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    override fun onImageClick(view: View) {
        super.onImageClick(view)
        if (isAnimating) return
        index++
        val name = when (index % 8) {
            0 -> { startWithInterpolator(LinearInterpolator(), "线性：匀速 Y=T"); "LinearInterpolator（线性插值器）" }
            1 -> { startWithInterpolator(AccelerateInterpolator(), "加速：y=t^(2f)"); "AccelerateInterpolator（加速插值器）" }
            2 -> { startWithInterpolator(DecelerateInterpolator(), "减速：y=1-(1-t)^(2f)"); "DecelerateInterpolator（减速插值器）" }
            3 -> { startWithInterpolator(AccelerateDecelerateInterpolator(), "先加速后减速"); "AccelerateDecelerateInterpolator（先加速后减速）" }
            4 -> { startWithInterpolator(BounceInterpolator(), "弹跳"); "BounceInterpolator（弹跳插值器）" }
            5 -> { startWithInterpolator(OvershootInterpolator(), "过冲：超过终点后回弹"); "OvershootInterpolator（过冲插值器）" }
            6 -> { startWithInterpolator(AnticipateInterpolator(), "回拉：先反向再前进"); "AnticipateInterpolator（回拉插值器）" }
            else -> { startViewPropertyAnimator(); "ViewPropertyAnimator（链式调用）" }
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }

    /**
     * ValueAnimator + 插值器
     * addUpdateListener 在每一帧回调，可获取当前插值后的值
     */
    private fun startWithInterpolator(interpolator: android.view.animation.Interpolator, label: String) {
        isAnimating = true
        mBinding.basicsImage.rotation = 0f
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            this.interpolator = interpolator
            addUpdateListener { animation ->
                mBinding.basicsImage.rotation = animation.animatedValue as Float
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
     * ViewPropertyAnimator — 链式调用
     * 比 ObjectAnimator 更简洁的写法，直接在 View 上调用 animate()
     * 适合简单的属性动画组合，代码更易读
     */
    private fun startViewPropertyAnimator() {
        isAnimating = true
        mBinding.basicsImage.animate()
            .translationX(-400f)
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(1500)
            .withEndAction {
                // 动画结束后恢复初始状态
                mBinding.basicsImage.animate()
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
