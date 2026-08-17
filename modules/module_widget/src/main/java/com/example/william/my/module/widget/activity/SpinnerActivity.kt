package com.example.william.my.module.widget.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.spinner.Spinner

/**
 * 下拉菜单 Spinner 示例
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