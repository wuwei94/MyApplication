package com.example.william.my.module.arch.mavericks

import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.base.recycler.BaseRecyclerFragment
import com.example.william.my.module.arch.adapter.ArticleAdapter
import com.example.william.my.module.arch.mavericks.data.ArticleMavericksState
import com.example.william.my.module.arch.mavericks.viewmodel.ArticleMavericksViewModel

/**
 * Mavericks 文章列表页面
 *
 * 演示使用 Mavericks 进行状态持久化、异步请求与分页列表数据绑定。
 */
class ArticleMavericksFragment : BaseRecyclerFragment<ArticleDetailData>(), MavericksView {

    /**
     * Mavericks 架构专属的 ViewModel 委托方式（区别于标准 Jetpack 的 by viewModels）：
     * 1. 自动处理 MavericksState 的持久化、跨进程恢复以及与 MavericksView 的生命周期绑定；
     * 2. 底层通过反射查找 ArticleMavericksViewModel.Companion 中实现的 MavericksViewModelFactory 创建实例。
     */
    private val viewModel: ArticleMavericksViewModel by fragmentViewModel()

    override fun initRecyclerAdapter(): BaseQuickAdapter<ArticleDetailData, QuickViewHolder> {
        return ArticleAdapter(arrayListOf())
    }

    override fun observeViewModel() {
        viewModel.onAsync(
            ArticleMavericksState::articleResponse,
            deliveryMode = uniqueOnly("load_article"),
            onFail = {
                onDataFail()
            },
            onSuccess = { response ->
                if (response.isSuccess) {
                    onDataSuccess(response.data?.datas ?: emptyList())
                } else {
                    showToast(response.message.ifEmpty { "加载失败" })
                    onDataFail()
                }
            }
        )

        queryData()
    }

    override fun queryData() {
        super.queryData()
        viewModel.loadArticle(mPage)
    }

    /**
     * MavericksView 统一渲染入口
     *
     * 列表类型页面通常使用 onAsync 监听离散的数据到达并通知 Adapter 增量刷新；
     * 对于声明式全屏渲染场景，可在 invalidate() 中通过 withState(viewModel) 统一绘制视图。
     */
    override fun invalidate() {
    }
}
