package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.opensource.R
import com.lzf.easyfloat.EasyFloat

/**
 * EasyFloat — 悬浮窗解决方案
 *
 * EasyFloat 是一个轻量级的悬浮窗库，支持全局悬浮窗和应用内悬浮窗。
 *
 * 核心特性：
 * 1. 简单易用：链式调用，快速创建悬浮窗
 * 2. 全局悬浮窗：支持系统级悬浮窗（需要权限）
 * 3. 应用内悬浮窗：无需权限，应用内自由悬浮
 * 4. 丰富的自定义：支持拖拽、吸附、动画等
 *
 * 基本用法：
 * ```kotlin
 * // 显示悬浮窗
 * EasyFloat.with(context)
 *     .setLayout(R.layout.float_layout)
 *     .show()
 *
 * // 隐藏悬浮窗
 * EasyFloat.dismiss()
 * ```
 *
 * 适用场景：
 * - 悬浮菜单、快捷入口
 * - 客服悬浮球、反馈入口
 * - 画中画、小窗口播放
 *
 * https://github.com/princekin-f/EasyFloat
 */
@Route(path = RouterPath.OpenSource.EasyFloat)
class EasyFloatActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示 EasyFloat 悬浮窗")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("显示悬浮窗", "隐藏悬浮窗")
    }

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
            .setLayout(R.layout.open_layout_float)
            .show()
    }
}