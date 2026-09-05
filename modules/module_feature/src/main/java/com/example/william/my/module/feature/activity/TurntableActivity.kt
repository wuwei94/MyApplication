package com.example.william.my.module.feature.activity

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import kotlin.random.Random

/**
 * 转盘抽奖 — 旋转动画实现
 *
 * 通过 RotateAnimation 实现转盘抽奖功能，支持指定奖品和随机抽奖。
 *
 * 核心特性：
 * 1. 旋转动画：使用 RotateAnimation 实现平滑旋转
 * 2. 指定奖品：支持指定抽中特定奖品
 * 3. 随机抽奖：支持随机抽奖功能
 * 4. 动画控制：防止重复触发，支持重置
 *
 * 核心原理：
 * 1. 使用 RotateAnimation 配合 Animation.RELATIVE_TO_SELF 设置以自身中心点 (0.5f, 0.5f) 为轴心旋转
 * 2. 角度计算：保持累加角度，确保每次抽奖至少旋转 MIN_LAPS 圈并平滑减速落在目标扇区
 * 3. 使用 android.R.anim.accelerate_decelerate_interpolator 先加速后减速，模拟真实物理阻尼效果
 *
 * 角度计算公式：
 * ```kotlin
 * val targetAngle = prizeIndex * 360f / TOTAL_PRIZES
 * val currentMod = (currentDegree % 360f + 360f) % 360f
 * val deltaToTarget = (targetAngle - currentMod + 360f) % 360f
 * val toDegree = currentDegree + MIN_LAPS * 360f + deltaToTarget
 * ```
 *
 * 适用场景：
 * - 抽奖活动、转盘游戏
 * - 随机选择、抽奖功能
 * - 需要旋转动画的场景
 */
@Route(path = RouterPath.Feature.Turntable)
class TurntableActivity : BasicImageActivity() {

    private var currentDegree = 0f // 当前停靠角度
    private var isAnimating = false // 动画执行状态标志，防止重复触发

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initTurntable()
    }

    private fun initTurntable() {
        mBinding.basicsImage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.shared_ic_launcher,
            ),
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "随机抽奖（旋转动画）",
        "指定抽中奖品 1（0°）",
        "指定抽中奖品 4（108°）",
        "指定抽中奖品 7（216°）",
        "重置转盘（恢复 0°）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> goAward(Random.nextInt(TOTAL_PRIZES))
            1 -> goAward(0)
            2 -> goAward(3)
            3 -> goAward(6)
            4 -> resetTurntable()
        }
    }

    /**
     * 执行抽奖旋转动画
     *
     * @param prizeIndex 奖品索引 (0 until TOTAL_PRIZES)
     */
    private fun goAward(prizeIndex: Int) {
        if (isAnimating) {
            Utils.toast("转盘正在旋转中，请稍候...")
            return
        }

        // 1. 计算目标奖品在 360° 圆周中的相对角度
        val targetAngle = prizeIndex * 360f / TOTAL_PRIZES

        // 2. 计算从当前停靠角到达目标角所需的顺时针增量角度（确保至少旋转 MIN_LAPS 圈）
        val currentMod = (currentDegree % 360f + 360f) % 360f
        val deltaToTarget = (targetAngle - currentMod + 360f) % 360f
        val toDegree = currentDegree + MIN_LAPS * 360f + deltaToTarget

        // 3. 构建旋转动画，相对自身中心 (0.5f, 0.5f) 旋转
        val rotateAnimation = RotateAnimation(
            currentDegree,
            toDegree,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
        ).apply {
            duration = ANIMATION_DURATION
            fillAfter = true // 动画结束后停留在最终角度
            setInterpolator(
                this@TurntableActivity,
                android.R.anim.accelerate_decelerate_interpolator,
            )
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animation) {
                    isAnimating = false
                    // 记录最终角度（取模保持数值稳定）
                    currentDegree = toDegree % 360f
                    Utils.toast("抽奖完成：恭喜抽中奖品 ${prizeIndex + 1}（${targetAngle.toInt()}°）")
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }

        mBinding.basicsImage.startAnimation(rotateAnimation)
    }

    /**
     * 重置转盘状态与角度
     */
    private fun resetTurntable() {
        if (isAnimating) return
        mBinding.basicsImage.clearAnimation()
        currentDegree = 0f
        Utils.toast("转盘已重置为初始状态（0°）")
    }

    companion object {
        private const val TOTAL_PRIZES = 10 // 奖品总数目
        private const val MIN_LAPS = 3 // 每次抽奖最小旋转圈数
        private const val ANIMATION_DURATION = 3000L // 动画播放总时间 (ms)
    }
}
