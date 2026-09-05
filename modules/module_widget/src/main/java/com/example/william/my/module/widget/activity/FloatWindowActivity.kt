package com.example.william.my.module.widget.activity

import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.widget.R
import com.example.william.my.module.widget.floatwindow.FloatTouchHelper

/**
 * 悬浮窗 — 系统级悬浮窗实现
 *
 * 通过 WindowManager 实现系统级悬浮窗，支持拖拽和吸附效果。
 *
 * 核心要点：
 * 1. 需申请 SYSTEM_ALERT_WINDOW 权限（android.permission.SYSTEM_ALERT_WINDOW）；
 * 2. 使用 TYPE_APPLICATION_OVERLAY（API 26+）或 TYPE_PHONE（旧版）；
 * 3. FLAG_NOT_FOCUSABLE 确保不拦截输入焦点；
 * 4. 通过 FloatTouchHelper 实现拖拽与边缘自动吸附。
 */
@Route(path = RouterPath.Widget.FloatWindow)
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

    override fun buildList(): ArrayList<String> = arrayListOf("显示/隐藏 悬浮窗")

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

        mLayoutParams?.flags = (
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 覆盖状态栏
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 不获取焦点
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            ) // 允许window之外点击事件传递给其他在其之后的window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun initFloatWindow() {
        mFloatWindow = LayoutInflater.from(this)
            .inflate(R.layout.ui_layout_float_window, window.decorView as ViewGroup, false)

        mFloatWindow?.let { float ->
            float.setOnTouchListener(
                FloatTouchHelper(
                    windowManager = mWindowManager,
                    layoutParams = mLayoutParams,
                ),
            )

            float.setOnClickListener {
                appendLog("点击了悬浮窗")
            }
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
