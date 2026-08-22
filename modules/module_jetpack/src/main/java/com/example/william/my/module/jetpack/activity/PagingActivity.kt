package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.jetpack.paging.adapter.PagingAdapter
import com.example.william.my.module.jetpack.paging.adapter.PagingStateAdapter
import com.example.william.my.module.jetpack.paging.viewmodel.PagingViewModel
import android.annotation.SuppressLint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Paging — 分页加载框架
 *
 * Paging 3 是 Android Jetpack 提供的分页加载框架，用于高效加载海量数据并适配多样化的数据源。
 *
 * 核心特性：
 * 1. 高效加载：只加载当前页面需要的数据，减少内存占用与网络流量
 * 2. 自动分页：自动处理分页位置与上拉加载更多、下拉刷新
 * 3. 离线缓存机制：支持 RemoteMediator + Room 实现离线优先与网络缓存同步
 * 4. 多数据源与响应式支持：原生支持 Kotlin Flow 与 RxJava Flowable
 *
 * 核心组件：
 * 1. PagingSource：数据源，负责定义分页加载协议
 * 2. RemoteMediator：远程协调器，负责网络数据向本地数据库的落库与同步
 * 3. Pager：分页器，负责将数据源配置并转化为 PagingData 流
 * 4. PagingDataAdapter：RecyclerView 适配器，配合 DiffUtil 局部增量渲染
 *
 * 基本用法：
 * ```kotlin
 * // 创建 Pager
 * val pager = Pager(PagingConfig(pageSize = 20)) {
 *     MyPagingSource()
 * }.flow.cachedIn(viewModelScope)
 *
 * // 在 Activity 中提交数据
 * lifecycleScope.launch {
 *     viewModel.items.collectLatest { pagingData ->
 *         adapter.submitData(pagingData)
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 列表分页加载与上拉加载更多
 * - 网络 + 本地数据库离线优先缓存场景
 * - 大量数据流式展示
 *
 * https://developer.android.google.cn/topic/libraries/architecture/paging/v3-overview
 */
@Route(path = RouterPath.Jetpack.Paging)
class PagingActivity : BasicRecyclerActivity() {

    private val mViewModel: PagingViewModel by viewModels {
        PagingViewModel.Factory
    }

    private lateinit var mAdapter: PagingAdapter
    private var flowJob: Job? = null
    private var rxDisposable: Disposable? = null

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Mediator (Flow 离线缓存流)",
            "Mediator (RxJava 离线缓存流)",
            "Network (Flow 纯网络流)",
            "Network (RxJava 纯网络流)",
            "刷新当前列表 (adapter.refresh())"
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPagingAdapter()
        switchMode(PagingMode.MEDIATOR_FLOW)
    }

    private fun initPagingAdapter() {
        mAdapter = PagingAdapter(PagingAdapter.PagingComparator())

        // 监听加载状态
        mAdapter.addLoadStateListener { loadStates ->
            when (val refresh = loadStates.refresh) {
                is LoadState.NotLoading -> Utils.logcat("Paging", "刷新完成 (NotLoading)")
                is LoadState.Loading -> Utils.logcat("Paging", "正在加载首页数据 (Loading)")
                is LoadState.Error -> Utils.logcat("Paging", "加载出错: ${refresh.error.message}")
            }
        }

        // 绑定 Header 与 Footer 加载状态
        mDataRecycler.layoutManager = LinearLayoutManager(this)
        mDataRecycler.adapter = mAdapter.withLoadStateHeaderAndFooter(
            header = PagingStateAdapter(mAdapter::retry),
            footer = PagingStateAdapter(mAdapter::retry)
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> switchMode(PagingMode.MEDIATOR_FLOW)
            1 -> switchMode(PagingMode.MEDIATOR_RX)
            2 -> switchMode(PagingMode.NETWORK_FLOW)
            3 -> switchMode(PagingMode.NETWORK_RX)
            4 -> mAdapter.refresh()
        }
    }

    /**
     * 动态切换数据流模式
     *
     * @SuppressLint("AutoDispose") — AutoDispose 2.2.1 使用 Kotlin 1.9.0 编译，
     * 与当前项目的 Kotlin 2.2.20 不兼容（编译时找不到 com.uber 包）。
     * 此处手动管理 Disposable 生命周期，在 onDestroy() 中 dispose。
     */
    @SuppressLint("AutoDispose")
    private fun switchMode(mode: PagingMode) {
        // 取消先前的流订阅，避免多流并发提交
        flowJob?.cancel()
        flowJob = null
        rxDisposable?.dispose()
        rxDisposable = null

        when (mode) {
            PagingMode.MEDIATOR_FLOW -> {
                flowJob = lifecycleScope.launch {
                    mViewModel.articlesByMediatorFlow.collectLatest { pagingData ->
                        mAdapter.submitData(pagingData)
                    }
                }
            }

            PagingMode.MEDIATOR_RX -> {
                rxDisposable = mViewModel.articlesByMediatorFlowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { pagingData ->
                        mAdapter.submitData(lifecycle, pagingData)
                    }
            }

            PagingMode.NETWORK_FLOW -> {
                flowJob = lifecycleScope.launch {
                    mViewModel.articlesByNetworkFlow.collectLatest { pagingData ->
                        mAdapter.submitData(pagingData)
                    }
                }
            }

            PagingMode.NETWORK_RX -> {
                rxDisposable = mViewModel.articlesByNetworkFlowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { pagingData ->
                        mAdapter.submitData(lifecycle, pagingData)
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flowJob?.cancel()
        rxDisposable?.dispose()
    }

    enum class PagingMode {
        MEDIATOR_FLOW,
        MEDIATOR_RX,
        NETWORK_FLOW,
        NETWORK_RX
    }
}