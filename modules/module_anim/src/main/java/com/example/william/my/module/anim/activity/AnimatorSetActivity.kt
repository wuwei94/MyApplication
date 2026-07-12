package com.example.william.my.module.anim.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * AnimatorSet — 动画组合
 *
 * 将多个 Animator 组合在一起，控制它们的播放顺序和并发关系。
 * 比 AnimatorSet.playSequentially() 更精细的编排可用 AnimatorSet.Builder。
 *
 * 三种组合方式：
 * 1. playSequentially() — 顺序播放
 * 2. playTogether() — 同时播放
 * 3. AnimatorSet.Builder — 精细编排（with / before / after）
 */
@Route(path = RouterPath.Anim.AnimatorSet)
class AnimatorSetActivity : BasicImageActivity() {

    private var index = -1

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    override fun onImageClick(view: View) {
        super.onImageClick(view)
        index++
        val name = when (index % 3) {
            0 -> { playSequentially(); "playSequentially（顺序播放）" }
            1 -> { playTogether(); "playTogether（同时播放）" }
            else -> { playWithBuilder(); "Builder（精细编排）" }
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }

    /**
     * playSequentially() — 顺序播放
     * 动画按传入顺序依次执行，前一个结束后才开始下一个
     */
    private fun playSequentially() {
        val set = AnimatorSet()
        set.playSequentially(createAlpha(), createRotation(), createScaleX())
        set.start()
    }

    /**
     * playTogether() — 同时播放
     * 所有动画在同一时刻开始，同时执行
     */
    private fun playTogether() {
        val set = AnimatorSet()
        set.playTogether(createAlpha(), createRotation(), createScaleX())
        set.start()
    }

    /**
     * AnimatorSet.Builder — 精细编排
     * play(a).with(b)    — a 和 b 同时播放
     * play(a).before(b)  — a 在 b 之前播放
     * play(a).after(b)   — a 在 b 之后播放
     *
     * 本例：alpha 和 rotation 同时播放，然后 scaleX 再播放
     */
    private fun playWithBuilder() {
        val alpha = createAlpha()
        val rotation = createRotation()
        val scaleX = createScaleX()
        val set = AnimatorSet()
        set.play(alpha).with(rotation)
        set.play(scaleX).after(alpha)
        set.start()
    }

    private fun createAlpha() =
        ObjectAnimator.ofFloat(mBinding.basicsImage, "alpha", 1f, 0f, 1f).setDuration(1000)

    private fun createRotation() =
        ObjectAnimator.ofFloat(mBinding.basicsImage, "rotation", 0f, 360f).setDuration(1000)

    private fun createScaleX() =
        ObjectAnimator.ofFloat(mBinding.basicsImage, "scaleX", 1f, 0.5f, 1f).setDuration(1000)
}
