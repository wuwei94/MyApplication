package com.example.william.my.module.anim.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * ObjectAnimator — 属性动画
 *
 * 通过反射调用 View 的 setter 方法（如 setAlpha、setRotation）来实现动画。
 * 最常用的属性动画 API，适用于对单个 View 属性做动画。
 *
 * 核心方法：ObjectAnimator.ofFloat(target, propertyName, values)
 */
@Route(path = RouterPath.Anim.ObjectAnimator)
class ObjectAnimatorActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Alpha（透明度）",
            "Rotation（旋转）",
            "ScaleX（缩放）",
            "TranslationX（平移）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startAlpha()
            1 -> startRotation()
            2 -> startScale()
            3 -> startTranslation()
        }
    }

    /**
     * 透明度动画
     * 属性名 "alpha" 对应 View.setAlpha()
     * 值范围 0.0（全透明）~ 1.0（不透明）
     */
    private fun startAlpha() {
        ObjectAnimator.ofFloat(mBinding.basicsImage, "alpha", 1f, 0f, 1f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 旋转动画
     * 属性名 "rotation" 对应 View.setRotation()
     * 值为旋转角度，0f → 360f 为顺时针一圈
     */
    private fun startRotation() {
        ObjectAnimator.ofFloat(mBinding.basicsImage, "rotation", 0f, 360f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 缩放动画
     * 属性名 "scaleX" 对应 View.setScaleX()
     * 1f 为原始大小，0.5f 为一半，2f 为两倍
     */
    private fun startScale() {
        ObjectAnimator.ofFloat(mBinding.basicsImage, "scaleX", 1f, 0.5f, 1f).apply {
            duration = 3000
            start()
        }
    }

    /**
     * 平移动画
     * 属性名 "translationX" 对应 View.setTranslationX()
     * 值为相对于原始位置的偏移量（像素）
     */
    private fun startTranslation() {
        ObjectAnimator.ofFloat(
            mBinding.basicsImage,
            "translationX",
            mBinding.basicsImage.translationX,
            -400f,
            mBinding.basicsImage.translationX
        ).apply {
            duration = 3000
            start()
        }
    }
}
