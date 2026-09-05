package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.spinner.Spinner
import com.example.william.my.module.widget_custom.R

/**
 * Spinner — 下拉菜单控件
 *
 * 下拉菜单控件，支持弹出式列表选择与锚点宽度自适应。
 *
 * 核心特性：
 * 1. 弹出式列表：支持从指定锚点 View 下方展开浮层
 * 2. 自定义样式：支持自定义列表项样式与高亮选中
 * 3. 点击事件：支持列表项点击回调与数据回填
 * 4. 宽度自适应：支持根据锚点 View 自适应宽度
 */
@Route(path = RouterPath.WidgetCustom.Spinner)
class SpinnerActivity : BasicLayoutActivity() {

    private lateinit var anchorView: View
    private lateinit var selectedTextView: TextView
    private lateinit var resultTextView: TextView

    private var mSpinner: Spinner? = null
    private val mData = arrayOf("Kotlin 协程开发", "Jetpack Compose 进阶", "Android 性能优化实战", "Flutter 混合工程架构")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPreviewCard()
    }

    private fun initPreviewCard() {
        val previewView = LayoutInflater.from(this)
            .inflate(R.layout.widget_layout_spinner_preview, mContainer, false)

        anchorView = previewView.findViewById(R.id.widget_spinner_anchor)
        selectedTextView = previewView.findViewById(R.id.widget_spinner_selected_text)
        resultTextView = previewView.findViewById(R.id.widget_spinner_result)

        anchorView.setOnClickListener {
            showSpinner()
        }

        setView(previewView)
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 展开 Spinner 下拉列表（锚定上方选择框）",
        "2. 重置选择器状态",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> showSpinner()
            1 -> resetSpinner()
        }
    }

    private fun showSpinner() {
        mSpinner = Spinner(this@SpinnerActivity, mData.toList())
        mSpinner?.width = anchorView.width
        mSpinner?.showAsDropDown(anchorView)
        mSpinner?.setItemListener { position ->
            val selected = mData[position]
            selectedTextView.text = selected
            resultTextView.text = "当前已选：$selected（索引: $position）"
        }
    }

    private fun resetSpinner() {
        selectedTextView.text = "请选择所属类别..."
        resultTextView.text = "当前状态：已重置为未选择"
    }
}
