package com.example.william.my.core.base.arch.mvp

import android.view.View

/**
 * 空/错误状态视图接口（用于列表等页面的加载中、空数据、错误态展示）
 */
interface IEmptyView {
    val rootView: View
    fun showEmptyView()
    fun showErrorView()
    fun hide()
    fun setOnClickListener(listener: OnEmptyClickListener?)
    interface OnEmptyClickListener {
        fun onRefresh()
    }
}
