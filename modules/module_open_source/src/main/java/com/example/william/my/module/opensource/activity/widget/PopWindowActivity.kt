package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseBinding
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * PopupWindow 演示
 */
@Route(path = RouterPath.OpenSource.PopWindow)
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