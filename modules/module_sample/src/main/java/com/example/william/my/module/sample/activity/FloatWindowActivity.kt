package com.example.william.my.module.sample.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.sample.R
import kotlin.math.abs

/**
 * 悬浮窗 — 系统级悬浮窗实现
 *
 * 通过 WindowManager 实现系统级悬浮窗，支持拖拽和吸附效果。
 *
 * 核心特性：
 * 1. 系统级悬浮窗：使用 WindowManager 添加全局悬浮窗
 * 2. 拖拽支持：支持手势拖拽移动位置
 * 3. 吸附效果：松手后自动吸附到屏幕边缘
 * 4. 权限处理：Android 6.0+ 需要悬浮窗权限
 *
 * 实现原理：
 * 1. 创建 WindowManager.LayoutParams，设置窗体类型和标志
 * 2. 使用 WindowManager.addView() 添加悬浮窗
 * 3. 通过 OnTouchListener 监听触摸事件，实现拖拽
 * 4. 松手时计算位置，使用 ValueAnimator 实现吸附动画
 *
 * 基本用法：
 * ```kotlin
 * // 创建悬浮窗
 * val floatView = LayoutInflater.from(context).inflate(R.layout.float_layout, null)
 * val params = WindowManager.LayoutParams().apply {
 *     type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
 *     flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
 * }
 *
 * // 添加到窗口
 * windowManager.addView(floatView, params)
 * ```
 *
 * 适用场景：
 * - 悬浮菜单、快捷入口
 * - 客服悬浮球、反馈入口
 * - 画中画、小窗口播放
 */
@Route(path = RouterPath.Sample.FloatWindow)
class FloatWindowActivity : BasicResponseActivity() {

    private var mFloatWindow: View? = null
    private var mWindowManager: WindowManager? = null
    private var mLayoutParams: WindowManager.LayoutParams? = null

    private var isShow = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示/隐藏悬浮窗")
        initFloatParams()
        initFloatWindow()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("显示/隐藏 悬浮窗")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            showFloat()
        }
    }

    private fun initFloatParams() {

        mLayoutParams = WindowManager.LayoutParams()

        // 设置宽高
        mLayoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        mLayoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT

        // 设置背景透明
        mLayoutParams?.format = PixelFormat.TRANSPARENT

        // 设置屏幕左上角为起始点
        mLayoutParams?.gravity = Gravity.START or Gravity.TOP


        mLayoutParams?.flags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN //覆盖状态栏
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  //不获取焦点
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL) //允许window之外点击事件传递给其他在其之后的window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // 设置窗体显示类型(TYPE_TOAST:与toast一个级别)
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_TOAST
        }
    }

    private fun initFloatWindow() {
        mFloatWindow = LayoutInflater.from(this)
            .inflate(R.layout.sample_layout_float_window, window.decorView as ViewGroup, false)

        mFloatWindow?.let { float ->
            float.setOnTouchListener(object : View.OnTouchListener {

                var startX = 0
                var startY = 0
                var isPerformClick = false //是否点击
                var finalMoveX = 0 //最后通过动画将mView的X轴坐标移动到finalMoveX
                val mTouchSlop = ViewConfiguration.get(this@FloatWindowActivity).scaledTouchSlop

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = event.x.toInt()
                            startY = event.y.toInt()
                            isPerformClick = true
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            //判断是CLICK还是MOVE
                            //只要移动过，就认为不是点击
                            if (abs(startX - event.x) >= mTouchSlop || abs(startY - event.y) >= mTouchSlop) {
                                isPerformClick = false
                            }

                            mLayoutParams?.let { params ->
                                params.x = (event.rawX - startX).toInt()
                                params.y = (event.rawY - startY).toInt()
                            }

                            updateFloatWindow()
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            if (isPerformClick) {
                                float.performClick()
                            }

                            //判断mView是在Window中的位置，以中间为界
                            mLayoutParams?.let { params ->
                                finalMoveX =
                                    if (params.x + float.measuredWidth / 2 >= resources.displayMetrics.widthPixels / 2) {
                                        resources.displayMetrics.widthPixels - float.measuredWidth
                                    } else {
                                        0
                                    }
                            }

                            stickToSide()
                            return !isPerformClick
                        }

                        else -> {}
                    }
                    return false
                }

                private fun stickToSide() {
                    mLayoutParams?.let { params ->
                        val animator = ValueAnimator
                            .ofInt(params.x, finalMoveX)
                            .setDuration(abs(params.x - finalMoveX).toLong())
                        animator.interpolator = LinearInterpolator()
                        animator.addUpdateListener { animation ->
                            params.x = animation.animatedValue as Int
                            updateFloatWindow()
                        }
                        animator.start()
                    }
                }
            })

            float.setOnClickListener {
                appendLog("点击了悬浮窗")
            }
        }
    }

    private fun updateFloatWindow() {
        if (mFloatWindow != null && mLayoutParams != null) {
            mWindowManager?.updateViewLayout(mFloatWindow, mLayoutParams)
        }
    }

    private fun showFloat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                appendLog("请求悬浮窗权限...")
            } else {
                if (!isShow) {
                    isShow = true
                    showFloatWindow()
                    appendLog("显示悬浮窗")
                } else {
                    isShow = false
                    dismissFloatWindow()
                    appendLog("隐藏悬浮窗")
                }
            }
        }
    }

    private fun showFloatWindow() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mFloatWindow, mLayoutParams)
    }

    private fun dismissFloatWindow() {
        if (mFloatWindow != null && mWindowManager != null) {
            isShow = false
            mWindowManager?.removeView(mFloatWindow)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dismissFloatWindow()
    }
}
