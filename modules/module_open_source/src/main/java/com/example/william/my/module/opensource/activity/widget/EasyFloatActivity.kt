package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.opensource.R
import com.lzf.easyfloat.EasyFloat

/**
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