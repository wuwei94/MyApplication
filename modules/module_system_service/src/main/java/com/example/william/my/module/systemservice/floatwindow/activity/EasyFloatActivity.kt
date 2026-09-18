package com.example.william.my.module.systemservice.floatwindow.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.systemservice.R
import com.lzf.easyfloat.EasyFloat

/**
 * EasyFloat — 悬浮窗解决方案
 *
 * EasyFloat 是一个轻量级的悬浮窗库，支持全局悬浮窗和应用内悬浮窗。
 *
 * 核心机制与避坑点：
 * 1. 简单易用：链式调用，快速创建悬浮窗
 * 2. 全局悬浮窗：支持系统级悬浮窗（需要权限）
 * 3. 应用内悬浮窗：无需权限，应用内自由悬浮
 * 4. 丰富的自定义：支持拖拽、吸附、动画等
 *
 * 显示模式与权限（共 4 种显示模式、2 类权限归属）：
 * 1. CURRENT_ACTIVITY：只在当前 Activity 显示（默认值），应用内浮窗，无需权限
 * 2. FOREGROUND：仅应用前台时显示，系统级浮窗，需 SYSTEM_ALERT_WINDOW
 * 3. BACKGROUND：仅应用后台时显示，系统级浮窗，需 SYSTEM_ALERT_WINDOW
 * 4. ALL_TIME：不分前后台一直显示，系统级浮窗，需 SYSTEM_ALERT_WINDOW
 *
 * 分界线在 EasyFloat.Builder.show() 的分支顺序上：只有 CURRENT_ACTIVITY 直接创建、跳过权限检查，
 * 其余三种经 PermissionUtils.checkPermission()，未授权时由库内无界面 Fragment 跳转系统授权页申请。
 * 本页沿用默认的 CURRENT_ACTIVITY，演示的是免权限的应用内浮窗。
 *
 * https://github.com/princekin-f/EasyFloat
 */
@Route(path = RouterPath.SystemService.EasyFloat)
class EasyFloatActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示 EasyFloat 悬浮窗")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("显示悬浮窗", "隐藏悬浮窗")

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                showEasyFloat()
                appendLog("显示 EasyFloat 悬浮窗")
            }

            1 -> {
                EasyFloat.dismiss()
                appendLog("隐藏 EasyFloat 悬浮窗")
            }
        }
    }

    private fun showEasyFloat() {
        EasyFloat.with(this)
            .setLayout(R.layout.systemservice_layout_float)
            .show()
    }
}
