package com.example.william.my.module.sample.floatwindow

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import kotlin.math.abs

/**
 * 悬浮窗拖拽与边缘自动吸附手势辅助类
 *
 * 封装 View.OnTouchListener：
 * 1. 触摸判定：区分手势点击与滑动拖拽（基于 ViewConfiguration.scaledTouchSlop）
 * 2. 实时拖拽：跟随手指移动动态更新 WindowManager.LayoutParams 坐标
 * 3. 松手吸附：以屏幕中线为界，自动吸附至左边缘或右边缘（带平滑动画）
 */
class FloatTouchHelper(
    private val windowManager: WindowManager?,
    private val layoutParams: WindowManager.LayoutParams?,
    private val onUpdateLayout: (() -> Unit)? = null
) : View.OnTouchListener {

    private var startX = 0
    private var startY = 0
    private var isPerformClick = false
    private var finalMoveX = 0

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val touchSlop = ViewConfiguration.get(v.context).scaledTouchSlop

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x.toInt()
                startY = event.y.toInt()
                isPerformClick = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 判断是 CLICK 还是 MOVE，位移超出 touchSlop 则判定为拖拽
                if (abs(startX - event.x) >= touchSlop || abs(startY - event.y) >= touchSlop) {
                    isPerformClick = false
                }

                layoutParams?.let { params ->
                    params.x = (event.rawX - startX).toInt()
                    params.y = (event.rawY - startY).toInt()
                }

                updateLayout(v)
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isPerformClick) {
                    v.performClick()
                }

                // 判断 View 是在 Window 中的位置，以屏幕中心为界计算吸附边缘
                val screenWidth = v.resources.displayMetrics.widthPixels
                layoutParams?.let { params ->
                    finalMoveX = if (params.x + v.measuredWidth / 2 >= screenWidth / 2) {
                        screenWidth - v.measuredWidth
                    } else {
                        0
                    }
                }

                stickToSide(v)
                return !isPerformClick
            }

            else -> {}
        }
        return false
    }

    /**
     * 平滑吸附到屏幕边缘
     */
    private fun stickToSide(v: View) {
        layoutParams?.let { params ->
            val animator = ValueAnimator
                .ofInt(params.x, finalMoveX)
                .setDuration(abs(params.x - finalMoveX).toLong())
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener { animation ->
                params.x = animation.animatedValue as Int
                updateLayout(v)
            }
            animator.start()
        }
    }

    private fun updateLayout(v: View) {
        if (layoutParams != null && windowManager != null) {
            windowManager.updateViewLayout(v, layoutParams)
        }
        onUpdateLayout?.invoke()
    }
}
