package com.example.william.my.module.anim.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * ObjectAnimator — 对象属性动画
 *
 * 通过反射调用 View 的 setter（如 setAlpha、setRotation）驱动属性动画，是对单个 View 单属性做动画时最常用的 API。
 *
 * 核心机制与避坑点：
 * 1. 反射驱动：按属性名映射到对应 setter，无需自定义 TypeEvaluator 也能跑通常见属性
 * 2. 关键帧序列：`ofFloat(target, propertyName, values)` 支持多段取值（如 1f → 0f → 1f）
 * 3. 时长可控：`duration` 以毫秒为单位，可与插值器组合
 * 4. 属性覆盖面广：alpha / rotation / scaleX / scaleY / translationX / translationY 等
 *
 * https://developer.android.google.cn/develop/ui/views/animations/prop-animation
 */
@Route(path = RouterPath.Anim.ObjectAnimator)
class ObjectAnimatorActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.shared_color_primary))
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "Alpha（透明度）",
        "Rotation（旋转）",
        "ScaleX（缩放）",
        "TranslationX（平移）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startAlpha()
            1 -> startRotation()
            2 -> startScale()
            3 -> startTranslation()
        }
    }

    /**
     * 透明度动画。
     *
     * 属性名 "alpha" 对应 View.setAlpha()；值范围 0.0（全透明）~ 1.0（不透明）。
     */
    private fun startAlpha() {
        ObjectAnimator.ofFloat(binding.basicsImage, "alpha", 1f, 0f, 1f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 旋转动画。
     *
     * 属性名 "rotation" 对应 View.setRotation()；值为旋转角度，0f → 360f 为顺时针一圈。
     */
    private fun startRotation() {
        ObjectAnimator.ofFloat(binding.basicsImage, "rotation", 0f, 360f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 缩放动画。
     *
     * 属性名 "scaleX" 对应 View.setScaleX()；1f 为原始大小，0.5f 为一半，2f 为两倍。
     */
    private fun startScale() {
        ObjectAnimator.ofFloat(binding.basicsImage, "scaleX", 1f, 0.5f, 1f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 平移动画。
     *
     * 属性名 "translationX" 对应 View.setTranslationX()；值为相对原始位置的像素偏移。
     */
    private fun startTranslation() {
        ObjectAnimator.ofFloat(
            binding.basicsImage,
            "translationX",
            binding.basicsImage.translationX,
            -400f,
            binding.basicsImage.translationX,
        ).apply {
            duration = 3000
            start()
        }
    }
}
