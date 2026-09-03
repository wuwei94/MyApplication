package com.example.william.my.core.base.arch.mvp

import android.view.View

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
