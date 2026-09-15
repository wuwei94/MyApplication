package com.example.william.my.module.anim.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * AnimatorSet — 动画组合与编排
 *
 * 将多个 Animator（ObjectAnimator / ValueAnimator 等）组合在一起，精确控制播放顺序与并发关系。
 *
 * 核心机制与避坑点：
 * 1. 顺序播放：`playSequentially(...)` 按传入顺序依次执行
 * 2. 同时播放：`playTogether(...)` 同一时刻启动全部动画
 * 3. Builder 编排：`play(a).with/before/after(b)` 表达复杂时序
 * 4. 可链式组合：支撑入场/出场等多段联动效果
 *
 * 单属性基础见 [ObjectAnimatorActivity]。
 *
 * https://developer.android.google.cn/develop/ui/views/animations/prop-animation#choreography
 */
@Route(path = RouterPath.Anim.AnimatorSet)
class AnimatorSetActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.basicsImage.setBackgroundColor(ContextCompat.getColor(this, R.color.shared_color_primary))
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "playSequentially（顺序播放）",
        "playTogether（同时播放）",
        "Builder（精细编排）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> playSequentially()
            1 -> playTogether()
            2 -> playWithBuilder()
        }
    }

    /**
     * playSequentially() 顺序播放。
     *
     * 动画按传入顺序依次执行，前一个结束后才开始下一个。
     */
    private fun playSequentially() {
        val set = AnimatorSet()
        set.playSequentially(createAlpha(), createRotation(), createScaleX())
        set.start()
    }

    /**
     * playTogether() 同时播放。
     *
     * 所有动画在同一时刻开始，同时执行。
     */
    private fun playTogether() {
        val set = AnimatorSet()
        set.playTogether(createAlpha(), createRotation(), createScaleX())
        set.start()
    }

    /**
     * AnimatorSet.Builder 精细编排。
     *
     * play(a).with(b) 与 b 同时播放；play(a).before(b) / after(b) 控制先后。
     * 本例：alpha 和 rotation 同时播放，然后 scaleX 再播放。
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

    private fun createAlpha() = ObjectAnimator.ofFloat(binding.basicsImage, "alpha", 1f, 0f, 1f).setDuration(1000)

    private fun createRotation() = ObjectAnimator.ofFloat(binding.basicsImage, "rotation", 0f, 360f).setDuration(1000)

    private fun createScaleX() = ObjectAnimator.ofFloat(binding.basicsImage, "scaleX", 1f, 0.5f, 1f).setDuration(1000)
}
