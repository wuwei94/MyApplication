package com.example.william.my.module.widget.activity

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
 * 核心机制与避坑点：
 * 1. 灵活定位：可相对于锚点 View 显示在任意位置
 * 2. 自定义布局：支持自定义布局内容
 * 3. 焦点控制：可设置是否获取焦点、是否可点击外部关闭
 * 4. 动画支持：可自定义显示/隐藏动画
 *
 * https://developer.android.com/reference/android/widget/PopupWindow
 */
@Route(path = RouterPath.Widget.PopWindow)
class PopWindowActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示 PopupWindow")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("显示 PopupWindow")

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
            true,
        ).apply {
            isOutsideTouchable = true
        }

        binding.basicsResponse.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.shared_color_primary,
            ),
        )
        binding.basicsResponse.text = "PopupWindow 内容区域\n点击关闭"
        binding.basicsResponse.setOnClickListener {
            appendLog("点击了 PopupWindow 内容区域")
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(binding.basicsResponse, 0, 0)
        appendLog("展示 PopupWindow")
    }
}
