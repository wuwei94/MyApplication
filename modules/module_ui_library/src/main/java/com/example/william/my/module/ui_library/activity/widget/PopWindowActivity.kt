package com.example.william.my.module.ui_library.activity.widget

import android.os.Bundle
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseBinding
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * PopupWindow — 弹出窗口演示
 *
 * PopupWindow 是 Android 原生的弹出窗口组件，可在任意位置显示浮层。
 *
 * 核心特性：
 * 1. 灵活定位：可相对于锚点 View 显示在任意位置
 * 2. 自定义布局：支持自定义布局内容
 * 3. 焦点控制：可设置是否获取焦点、是否可点击外部关闭
 * 4. 动画支持：可自定义显示/隐藏动画
 *
 * 基本用法：
 * ```kotlin
 * val popupWindow = PopupWindow(
 *     contentView,           // 内容布局
 *     width,                 // 宽度
 *     height,                // 高度
 *     focusable              // 是否可聚焦
 * )
 * popupWindow.isOutsideTouchable = true  // 点击外部可关闭
 * popupWindow.showAsDropDown(anchorView)  // 相对于锚点显示
 * ```
 *
 * 适用场景：
 * - 下拉菜单、筛选菜单
 * - 提示气泡、引导提示
 * - 自定义弹窗、对话框
 */
@Route(path = RouterPath.UiLibrary.PopWindow)
class PopWindowActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示 PopupWindow")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("显示 PopupWindow")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            showPopWindow()
        }
    }

    private fun showPopWindow() {
        val binding = SharedLayoutResponseBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(
            binding.root,
            resources.getDimensionPixelOffset(R.dimen.shared_dp_dialog_width),
            resources.getDimensionPixelOffset(R.dimen.shared_dp_dialog_height),
            true
        ).apply {
            isOutsideTouchable = true
        }

        binding.basicsResponse.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.shared_color_primary
            )
        )
        binding.basicsResponse.text = "PopupWindow 内容区域\n点击关闭"
        binding.basicsResponse.setOnClickListener {
            appendLog("点击了 PopupWindow 内容区域")
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(mBinding.basicsResponse, 0, 0)
        appendLog("展示 PopupWindow")
    }
}
