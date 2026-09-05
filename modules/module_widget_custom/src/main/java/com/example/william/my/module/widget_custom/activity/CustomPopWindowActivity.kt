package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.popwindow.CustomPopWindow
import com.example.william.my.module.widget_custom.R

/**
 * CustomPopWindow — 通用 PopupWindow 封装
 *
 * <a href="https://github.com/pinguo-zhouwei/CustomPopwindow">CustomPopWindow</a>
 * 自定义 PopWindow 类，封装了 PopupWindow 的一些常用属性，用 Builder 模式支持链式调用。
 * Created by zhouwei on 16/11/28.
 *
 * 注意：第三方原版持有的是 Context，本项目内嵌版（`com.example.william.my.core.widget.popwindow`）
 * 已将其改为 Activity：
 * <p>
 * private Context mContext; ——> private Activity mActivity;
 * <p>
 * 因此 PopupWindowBuilder 的构造参数是 Activity，使用时必须传入 Activity（如 this），
 * 不要误传 applicationContext / baseContext 等 Context，否则类型不匹配会出错。
 * 原因：PopupWindow 的展示依赖 Activity 的窗口（如背景变暗需要 Activity.getWindow()）。
 *
 * 核心特性：
 * 1. Builder 链式调用：像 AlertDialog 一样链式配置 PopupWindow
 * 2. 常用属性封装：焦点、外部触摸、动画、输入法模式、触摸拦截等
 * 3. 背景变暗：弹出时背景自动变暗，dismiss 时自动还原
 * 4. 兼容处理：兼容 Android 6.0+ 点击外部区域关闭
 *
 * 基本用法：
 * ```kotlin
 * val popWindow = CustomPopWindow.PopupWindowBuilder(this)
 *     .setView(R.layout.pop_layout)
 *     .enableBackgroundDark(true)   // 弹出时背景是否变暗
 *     .setBgDarkAlpha(0.7f)         // 变暗透明度（0-1）
 *     .create()
 *     .showAsDropDown(anchorView, 0, 0)
 * ```
 *
 * 适用场景：
 * - 顶部/底部菜单弹窗
 * - 筛选、下拉选项
 * - 自定义气泡、引导提示
 */
@Route(path = RouterPath.WidgetCustom.CustomPopWindow)
class CustomPopWindowActivity : BasicResponseActivity() {

    private var mCustomPopWindow: CustomPopWindow? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示 CustomPopWindow")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "显示 CustomPopWindow",
        "显示 CustomPopWindow（背景变暗）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> showCustomPopWindow(false)
            1 -> showCustomPopWindow(true)
        }
    }

    private fun showCustomPopWindow(backgroundDark: Boolean) {
        val contentView = layoutInflater.inflate(
            R.layout.widget_layout_custom_pop_window,
            null,
        )

        contentView.findViewById<TextView>(R.id.widget_item_one).setOnClickListener {
            mCustomPopWindow?.dismiss()
            appendLog("点击了菜单项 1")
        }
        contentView.findViewById<TextView>(R.id.widget_item_two).setOnClickListener {
            mCustomPopWindow?.dismiss()
            appendLog("点击了菜单项 2")
        }

        val builder = CustomPopWindow.PopupWindowBuilder(this)
            .setView(contentView)
            .setFocusable(true)
            .setOutsideTouchable(true)

        if (backgroundDark) {
            builder.enableBackgroundDark(true).setBgDarkAlpha(0.7f)
        }

        mCustomPopWindow = builder.create()
            .showAsDropDown(mBinding.basicsResponse, 0, 0)

        appendLog(if (backgroundDark) "展示 CustomPopWindow（背景变暗）" else "展示 CustomPopWindow")
    }
}
