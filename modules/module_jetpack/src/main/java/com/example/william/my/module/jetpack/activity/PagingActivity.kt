package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import autodispose2.AutoDispose
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.jetpack.databinding.JetpackActivityPagingBinding
import com.example.william.my.module.jetpack.paging.adapter.PagingAdapter
import com.example.william.my.module.jetpack.paging.adapter.PagingStateAdapter
import com.example.william.my.module.jetpack.paging.viewmodel.PagingViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Paging — 分页加载框架
 *
 * Paging 是 Android Jetpack 提供的分页加载框架，用于高效加载大量数据。
 *
 * 核心特性：
 * 1. 高效加载：只加载当前页面需要的数据，减少内存占用
 * 2. 自动分页：自动处理分页逻辑，支持上拉加载更多
 * 3. 缓存机制：支持数据缓存，提升用户体验
 * 4. 多数据源：支持网络、数据库、内存等多种数据源
 *
 * 核心组件：
 * 1. PagingSource：数据源，负责加载数据
 * 2. PagingData：分页数据，包含当前页的数据
 * 3. Pager：分页器，负责创建 PagingData
 * 4. PagingDataAdapter：适配器，负责绑定数据到 View
 *
 * 基本用法：
 * ```kotlin
 * // 创建 PagingSource
 * class MyPagingSource : PagingSource<Int, Item>() {
 *     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
 *         val page = params.key ?: 1
 *         val response = api.getItems(page, params.loadSize)
 *         return LoadResult.Page(
 *             data = response.items,
 *             prevKey = if (page == 1) null else page - 1,
 *             nextKey = if (response.items.isEmpty()) null else page + 1
 *         )
 *     }
 * }
 *
 * // 创建 Pager
 * val pager = Pager(PagingConfig(pageSize = 20)) {
 *     MyPagingSource()
 * }.flow
 *
 * // 在 ViewModel 中
 * val items: Flow<PagingData<Item>> = pager.cachedIn(viewModelScope)
 *
 * // 在 Activity 中
 * lifecycleScope.launch {
 *     viewModel.items.collectLatest { pagingData ->
 *         adapter.submitData(pagingData)
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 列表分页加载
 * - 下拉刷新、上拉加载更多
 * - 大量数据展示
 *
 * https://developer.android.google.cn/topic/libraries/architecture/paging/v3-overview
 */
@Route(path = RouterPath.Jetpack.Paging)
class PagingActivity : BaseVBActivity<JetpackActivityPagingBinding>() {

    private val mViewModel: PagingViewModel by viewModels {
        PagingViewModel.Factory
    }

    private lateinit var mAdapter: PagingAdapter

    override fun getViewBinding(): JetpackActivityPagingBinding {
        return JetpackActivityPagingBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPaging()
    }

    private fun initPaging() {
        mAdapter =
            PagingAdapter(PagingAdapter.PagingComparator())

        initArticlesByMediatorFlow(mViewModel, mAdapter)

        //获取加载状态
        mAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> {
                    Utils.logcat("Paging", "is NotLoading")
                }

                is LoadState.Loading -> {
                    Utils.logcat("Paging", "is Loading")
                }

                is LoadState.Error -> {
                    Utils.logcat("Paging", "is Error")
                }
            }
        }

        //呈现加载状态
        mAdapter.withLoadStateHeaderAndFooter(
            header = PagingStateAdapter(mAdapter::retry),
            footer = PagingStateAdapter(mAdapter::retry)
        )

        mBinding.pagingRecycleView.adapter = mAdapter
    }

    // =========================================================================
    // 1. 网络 + 数据库缓存（RemoteMediator 模式）
    // =========================================================================

    private fun initArticlesByMediatorLiveData(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        viewModel.articlesByMediatorLiveData.observe(this@PagingActivity) { pagingData ->
            lifecycleScope.launch {
                adapter.submitData(pagingData)
            }
        }
    }

    private fun initArticlesByMediatorFlow(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        lifecycleScope.launch {
            viewModel.articlesByMediatorFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun initArticlesByMediatorFlowable(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        viewModel.articlesByMediatorFlowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe {
                adapter.submitData(lifecycle, it)
            }
    }

    // =========================================================================
    // 2. 纯网络直接加载（PagingSource 模式）
    // =========================================================================

    private fun initArticlesByNetworkLiveData(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        viewModel.articlesByNetworkLiveData.observe(this@PagingActivity) { pagingData ->
            lifecycleScope.launch {
                adapter.submitData(pagingData)
            }
        }
    }

    private fun initArticlesByNetworkFlow(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        lifecycleScope.launch {
            viewModel.articlesByNetworkFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun initArticlesByNetworkFlowable(
        viewModel: PagingViewModel,
        adapter: PagingAdapter
    ) {
        viewModel.articlesByNetworkFlowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
            .subscribe {
                adapter.submitData(lifecycle, it)
            }
    }
}