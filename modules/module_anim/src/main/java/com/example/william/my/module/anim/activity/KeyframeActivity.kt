package com.example.william.my.module.anim.activity

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Keyframe + PropertyValuesHolder — 关键帧动画
 *
 * Keyframe: 定义动画在特定时间点的状态，系统自动补间中间过程
 * PropertyValuesHolder: 封装属性名+值序列，可同时驱动多个属性
 *
 * 核心方法：
 * - Keyframe.ofFloat(fraction, value) — fraction 为 0~1 的时间比例
 * - PropertyValuesHolder.ofKeyframe(propertyName, keyframes...) — 关键帧组合
 * - ObjectAnimator.ofPropertyValuesHolder(target, pvh...) — 用 PHV 驱动动画
 */
@Route(path = RouterPath.Anim.Keyframe)
class KeyframeActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "PropertyValuesHolder（多属性）",
            "Keyframe（关键帧）",
            "组合（旋转+缩放）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startPropertyValuesHolder()
            1 -> startKeyframe()
            2 -> startCombined()
        }
    }

    /**
     * PropertyValuesHolder — 同时对多个属性做动画
     * 比创建多个 ObjectAnimator 更高效，因为只创建一次动画器
     */
    private fun startPropertyValuesHolder() {
        val pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 2f, 1f)
        val pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 2f, 1f)
        val pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f)

        ObjectAnimator.ofPropertyValuesHolder(mBinding.basicsImage, pvhScaleX, pvhScaleY, pvhAlpha).apply {
            duration = 2000
            start()
        }
    }

    /**
     * Keyframe 关键帧动画
     * 自定义每个时间点的状态，系统自动补间
     * fraction: 0.0 = 开始, 0.5 = 中间, 1.0 = 结束
     */
    private fun startKeyframe() {
        // 旋转：0° → 90° → 0° → -90° → 0°（左右摇摆效果）
        val kf1 = Keyframe.ofFloat(0f, 0f)
        val kf2 = Keyframe.ofFloat(0.25f, 90f)
        val kf3 = Keyframe.ofFloat(0.5f, 0f)
        val kf4 = Keyframe.ofFloat(0.75f, -90f)
        val kf5 = Keyframe.ofFloat(1f, 0f)

        val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf1, kf2, kf3, kf4, kf5)

        ObjectAnimator.ofPropertyValuesHolder(mBinding.basicsImage, pvhRotation).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            start()
        }
    }

    /**
     * PropertyValuesHolder + Keyframe 组合
     * 同时驱动旋转和缩放，使用关键帧定义各自的时间线
     */
    private fun startCombined() {
        // 旋转关键帧：加速旋转再减速
        val kfRotation1 = Keyframe.ofFloat(0f, 0f)
        val kfRotation2 = Keyframe.ofFloat(0.3f, 180f)
        val kfRotation3 = Keyframe.ofFloat(0.7f, 540f)
        val kfRotation4 = Keyframe.ofFloat(1f, 720f)

        val pvhRotation = PropertyValuesHolder.ofKeyframe(
            "rotation", kfRotation1, kfRotation2, kfRotation3, kfRotation4
        )

        // 缩放关键帧：先放大再缩小再放大
        val kfScale1 = Keyframe.ofFloat(0f, 1f)
        val kfScale2 = Keyframe.ofFloat(0.5f, 2f)
        val kfScale3 = Keyframe.ofFloat(1f, 1f)

        val pvhScale = PropertyValuesHolder.ofKeyframe(
            "scaleX", kfScale1, kfScale2, kfScale3
        )

        val pvhScaleY = PropertyValuesHolder.ofKeyframe(
            "scaleY", kfScale1, kfScale2, kfScale3
        )

        ObjectAnimator.ofPropertyValuesHolder(
            mBinding.basicsImage, pvhRotation, pvhScale, pvhScaleY
        ).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            start()
        }
    }
}
