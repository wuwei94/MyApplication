package com.example.william.my.module.arch.mvp

import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.core.base.ui.recycler.BaseRecyclerFragment
import com.example.william.my.module.arch.adapter.ArticleAdapter
import com.example.william.my.module.arch.mvp.contract.ArticleContract
import com.example.william.my.module.arch.mvp.presenter.ArticlePresenter

/**
 * MVP：Model-View-Presenter
 *
 * View 层仅负责 UI 渲染与用户交互响应，所有业务与数据逻辑委托给 Presenter 处理。
 */
class MvpFragment :
    BaseRecyclerFragment<ArticleDetailData>(),
    ArticleContract.View {

    private var mPresenter: ArticlePresenter? = null

    override fun initRecyclerAdapter(): BaseQuickAdapter<ArticleDetailData, QuickViewHolder> = ArticleAdapter(arrayListOf())

    override fun observeViewModel() {
        super.observeViewModel()

        if (mPresenter == null) {
            mPresenter = ArticlePresenter(
                ServiceLocator.provideArticleRepository(requireActivity().applicationContext),
                this,
            )
        }

        queryData()
    }

    override fun queryData() {
        super.queryData()
        mPresenter?.loadArticle(mPage)
    }

    override fun showArticle(articles: List<ArticleDetailData>) {
        onDataSuccess(articles)
    }

    override fun showNoArticle() {
        onDataFail()
    }

    override fun onDestroyView() {
        mPresenter?.clear()
        mPresenter = null
        super.onDestroyView()
    }
}
