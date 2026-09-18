package com.example.william.my.module.systemservice.activity.floatwindow

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.net.toUri
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.systemservice.R

/**
 * 悬浮窗 — 系统级 WindowManager 悬浮窗实现
 *
 * 通过 WindowManager 添加 TYPE_APPLICATION_OVERLAY 窗口实现系统级悬浮窗，
 * 支持拖拽与边缘自动吸附。页面按调用顺序铺开完整生命周期：
 * 装配 LayoutParams → 权限判定与申请 → addView / removeView。
 *
 * 核心机制与避坑点：
 * 1. 系统级悬浮窗：WindowManager 添加 TYPE_APPLICATION_OVERLAY（API 26+）或 TYPE_PHONE（旧版）窗口，跨应用显示
 * 2. 叠加层权限：需申请 SYSTEM_ALERT_WINDOW（android.permission.SYSTEM_ALERT_WINDOW）
 * 3. 焦点隔离：FLAG_NOT_FOCUSABLE 确保不拦截输入焦点
 * 4. 拖拽与吸附：通过 FloatTouchHelper 实现拖拽与边缘自动吸附
 * 5. 授权流程：叠加层无 ActivityResult 契约可用，只能跳转系统授权页由用户手动开启，授权后再次点击本项即可展示
 *
 * https://developer.android.com/reference/android/view/WindowManager
 */
@Route(path = RouterPath.SystemService.FloatWindow)
class FloatWindowActivity : BasicResponseActivity() {

    private lateinit var windowManager: WindowManager

    private lateinit var windowParams: WindowManager.LayoutParams

    private lateinit var floatView: View

    private var isShow = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示/隐藏悬浮窗")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowParams = buildWindowParams()
        floatView = inflateFloatView()
    }

    override fun buildList(): ArrayList<String> = arrayListOf("显示/隐藏 悬浮窗")

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            toggleFloatWindow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 系统级窗口不随页面销毁而消失，必须主动移除，否则 WindowLeaked
        dismissFloatWindow()
    }

    /**
     * 展示 / 隐藏悬浮窗。
     *
     * 叠加层是特殊权限，没有 ActivityResult 契约可申请，只能跳转系统授权页手动开启；
     * 授权后返回本页再次点击本项即可展示。
     */
    private fun toggleFloatWindow() {
        if (isShow) {
            dismissFloatWindow()
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            appendLog("请求悬浮窗权限...")
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri()),
            )
            return
        }
        showFloatWindow()
    }

    /**
     * 装配窗口参数：宽高、透明背景、起始坐标、行为标志与窗口类型。
     */
    private fun buildWindowParams(): WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        // 窗口宽高包裹内容
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT

        // 背景透明
        format = PixelFormat.TRANSPARENT

        // 以屏幕左上角为起始点
        gravity = Gravity.START or Gravity.TOP

        flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 覆盖状态栏
            .or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) // 不获取焦点
            .or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL) // 允许window之外点击事件传递给其他在其之后的window

        // 系统级窗口类型：API 26 起统一用 TYPE_APPLICATION_OVERLAY，旧版用 TYPE_PHONE
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun inflateFloatView(): View {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.systemservice_layout_float_window, window.decorView as ViewGroup, false)
        // FloatTouchHelper 持有的是此处传入的 WindowManager 实例，故 windowManager 须先于监听就绪
        view.setOnTouchListener(FloatTouchHelper(windowManager, windowParams))
        view.setOnClickListener { appendLog("点击了悬浮窗") }
        return view
    }

    private fun showFloatWindow() {
        windowManager.addView(floatView, windowParams)
        isShow = true
        appendLog("显示悬浮窗")
    }

    private fun dismissFloatWindow() {
        if (!isShow) return
        windowManager.removeView(floatView)
        isShow = false
        appendLog("隐藏悬浮窗")
    }
}
