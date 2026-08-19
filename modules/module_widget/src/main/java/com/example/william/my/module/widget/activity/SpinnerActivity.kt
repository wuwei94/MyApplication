package com.example.william.my.module.widget.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.spinner.Spinner

/**
 * Spinner — 下拉菜单控件
 *
 * 下拉菜单控件，支持弹出式列表选择。
 *
 * 核心特性：
 * 1. 弹出式列表：支持从指定位置弹出列表
 * 2. 自定义样式：支持自定义列表项样式
 * 3. 点击事件：支持列表项点击回调
 * 4. 宽度自适应：支持根据锚点 View 自适应宽度
 *
 * 基本用法：
 * ```kotlin
 * // 创建 Spinner
 * val spinner = Spinner(context, dataList)
 * spinner.width = anchorView.width
 * spinner.showAsDropDown(anchorView)
 *
 * // 设置点击监听
 * spinner.setItemListener { position ->
 *     // 处理选择
 * }
 * ```
 *
 * 适用场景：
 * - 下拉选择菜单
 * - 筛选条件选择
 * - 自定义下拉列表
 */
@Route(path = RouterPath.Widget.Spinner)
class SpinnerActivity : BasicResponseActivity() {

    private var mSpinner: Spinner? = null
    private val mData = arrayOf("第一条数据", "第二条数据", "第三条数据", "第四条数据")

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项弹出下拉列表 Spinner")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("显示 Spinner 下拉列表")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            showSpinner()
        }
    }

    private fun showSpinner() {
        mSpinner = Spinner(this@SpinnerActivity, listOf(*mData))
        mSpinner?.width = mBinding.basicsResponse.width
        mSpinner?.showAsDropDown(mBinding.basicsResponse)
        mSpinner?.setItemListener { position ->
            appendLog("选择了: ${mData[position]}")
        }
        appendLog("弹出 Spinner 下拉列表")
    }
}