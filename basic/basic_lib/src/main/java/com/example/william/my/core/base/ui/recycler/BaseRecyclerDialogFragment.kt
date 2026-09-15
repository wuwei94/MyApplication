package com.example.william.my.core.base.ui.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.core.base.databinding.BaseFragmentRecyclerViewBinding
import com.example.william.my.core.base.ui.dialog.BaseVBDialogFragment
import com.example.william.my.core.base.ui.recycler.handler.BaseRecyclerHandler
import com.example.william.my.core.base.ui.recycler.host.RecyclerViewHost

/**
 * RecyclerView 列表 DialogFragment 基类（基于 BaseRecyclerViewAdapterHelper）
 *
 * 布局装配流水线：LayoutManager -> Adapter -> ItemDecoration -> OnScrollListener
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
abstract class BaseRecyclerDialogFragment<T : Any> :
    BaseVBDialogFragment<BaseFragmentRecyclerViewBinding>(),
    RecyclerViewHost<T> {

    private lateinit var recyclerHandler: BaseRecyclerHandler<T>

    override fun getViewBinding(): BaseFragmentRecyclerViewBinding = BaseFragmentRecyclerViewBinding.inflate(layoutInflater)

    override fun initView(view: View?, state: Bundle?) {
        super.initView(view, state)

        recyclerHandler = BaseRecyclerHandler(this)
        recyclerHandler.initRecyclerView()
        initRecyclerData(state)
    }

    // ===== RecyclerViewHost接口实现 =====

    override fun getHostBinding(): BaseFragmentRecyclerViewBinding = binding

    override fun getHostContext(): Context = requireContext()

    // ===== 配置方法 =====

    /**
     * 初始化RecyclerView状态视图
     */
    override fun initRecyclerViewStateView() {
        recyclerHandler.initRecyclerViewStateView()
    }

    // ===== 委托方法 =====

    /**
     * 数据加载成功处理
     */
    override fun onDataSuccess(list: List<T>?) {
        recyclerHandler.onDataSuccess(list)
    }

    /**
     * 数据加载失败处理
     */
    override fun onDataFail() {
        recyclerHandler.onDataFail()
    }

    /**
     * 滚动到顶部
     */
    override fun scrollToTop() {
        recyclerHandler.scrollToTop()
    }

    /**
     * 显示提示信息
     */
    override fun showToast(message: String?) {
        recyclerHandler.showToast(message)
    }

    // ===== 属性访问器 =====

    /**
     * 获取当前页码
     */
    val page: Int get() = recyclerHandler.page

    /**
     * 获取页面大小
     */
    val pageSize: Int get() = recyclerHandler.pageSize

    /**
     * 获取LayoutManager
     */
    val layoutManager: RecyclerView.LayoutManager? get() = recyclerHandler.layoutManager

    /**
     * 获取Adapter
     */
    val adapter: BaseQuickAdapter<T, QuickViewHolder>? get() = recyclerHandler.adapter

    /**
     * 获取多类型Adapter
     */
    val multiItemAdapter: BaseMultiItemAdapter<T>? get() = recyclerHandler.multiItemAdapter

    /**
     * 获取QuickAdapterHelper
     */
    val adapterHelper: QuickAdapterHelper? get() = recyclerHandler.adapterHelper
}
