package com.example.william.my.core.base.ui.recycler.handler

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.core.base.ui.recycler.host.RecyclerViewHost
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

/**
 * RecyclerView通用处理委托类
 * 抽离Fragment的公共业务逻辑，使用委托模式实现代码复用
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 * LayoutManager -> Adapter -> ItemDecoration -> OnScrollListener
 */
class BaseRecyclerHandler<T : Any>(
    private val host: RecyclerViewHost<T>,
) : BaseQuickAdapter.OnItemClickListener<T>,
    BaseQuickAdapter.OnItemChildClickListener<T>,
    BaseQuickAdapter.OnItemLongClickListener<T>,
    BaseQuickAdapter.OnItemChildLongClickListener<T>,
    OnRefreshLoadMoreListener {

    // ===== 分页相关 =====
    var page: Int = host.getStartPage()
    var pageSize: Int = 20

    // ===== RecyclerView相关组件 =====
    var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: BaseQuickAdapter<T, QuickViewHolder>? = null
    var multiItemAdapter: BaseMultiItemAdapter<T>? = null
    lateinit var adapterHelper: QuickAdapterHelper

    /**
     * 初始化RecyclerView
     */
    fun initRecyclerView() {
        val binding = host.getHostBinding()

        // 设置SmartRefreshLayout
        binding.smartRefresh.setEnableOverScrollDrag(true)
        binding.smartRefresh.setNestedScrollingEnabled(false)
        binding.smartRefresh.setEnableRefresh(host.canRefresh())
        binding.smartRefresh.setEnableLoadMore(host.canLoadMore())
        binding.smartRefresh.setOnRefreshLoadMoreListener(this)

        // 设置LayoutManager
        layoutManager = host.initRecyclerManager()
        layoutManager?.let {
            binding.recyclerView.layoutManager = it
        }

        // 设置Adapter
        adapter = host.initRecyclerAdapter()
        multiItemAdapter = host.initRecyclerMultiAdapter()

        adapter?.let {
            it.setOnItemClickListener(this)
            adapterHelper = QuickAdapterHelper.Builder(it).build()
            binding.recyclerView.adapter = adapterHelper.adapter
        }

        multiItemAdapter?.let {
            it.setOnItemClickListener(this)
            adapterHelper = QuickAdapterHelper.Builder(it).build()
            binding.recyclerView.adapter = adapterHelper.adapter
        }

        // 添加装饰器
        host.initItemDecoration().forEach {
            binding.recyclerView.addItemDecoration(it)
        }

        // 添加滚动监听器
        host.initOnScrollListener().forEach {
            binding.recyclerView.addOnScrollListener(it)
        }
    }

    /**
     * 设置RecyclerView状态视图
     */
    fun initRecyclerViewStateView() {
        if (host.emptyView() != null) {
            adapter?.isStateViewEnable = true
            multiItemAdapter?.isStateViewEnable = true
            adapter?.stateView = host.emptyView()
            multiItemAdapter?.stateView = host.emptyView()
        }

        if (host.emptyResId() != 0) {
            val context = host.getHostContext()
            adapter?.isStateViewEnable = true
            multiItemAdapter?.isStateViewEnable = true
            adapter?.setStateViewLayout(context, host.emptyResId())
            multiItemAdapter?.setStateViewLayout(context, host.emptyResId())
        }
    }

    /**
     * 数据加载成功处理（支持两种重载方式）
     */
    fun onDataSuccess(list: List<T>?) {
        val newList = list ?: emptyList()
        val binding = host.getHostBinding()

        if (page == host.getStartPage()) {
            adapter?.submitList(newList)
            multiItemAdapter?.submitList(newList)
        } else {
            adapter?.addAll(newList)
            multiItemAdapter?.addAll(newList)
        }

        initRecyclerViewStateView()

        if (newList.size < pageSize) {
            binding.smartRefresh.finishLoadMoreWithNoMoreData()
        } else {
            binding.smartRefresh.setEnableLoadMore(host.canLoadMore())
        }
        binding.smartRefresh.finishRefresh()
        binding.smartRefresh.finishLoadMore()
    }

    /**
     * 数据加载失败处理
     */
    fun onDataFail() {
        val binding = host.getHostBinding()
        binding.smartRefresh.finishRefresh(false)
        binding.smartRefresh.finishLoadMore(false)
        // 加载更多失败时回退页码，避免重试时跳过当前失败页
        if (page > host.getStartPage()) {
            page--
        }
    }

    /**
     * 滚动到顶部
     */
    fun scrollToTop() {
        host.getHostBinding().recyclerView.scrollToPosition(0)
    }

    /**
     * 显示提示信息
     */
    fun showToast(message: String?) {
        val context = host.getHostContext()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // ===== OnRefreshLoadMoreListener实现 =====

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = host.getStartPage()
        host.queryData()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        host.queryData()
    }

    // ===== 点击事件委托 =====

    override fun onClick(adapter: BaseQuickAdapter<T, *>, view: View, position: Int) {
        host.onClick(adapter, view, position)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<T, *>, view: View, position: Int) {
        host.onItemClick(adapter, view, position)
    }

    override fun onLongClick(
        adapter: BaseQuickAdapter<T, *>,
        view: View,
        position: Int,
    ): Boolean = host.onLongClick(adapter, view, position)

    override fun onItemLongClick(
        adapter: BaseQuickAdapter<T, *>,
        view: View,
        position: Int,
    ): Boolean = host.onItemLongClick(adapter, view, position)
}
