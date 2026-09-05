package com.example.william.my.module.arch.mvvm

import androidx.fragment.app.viewModels
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.base.ui.recycler.BaseRecyclerFragment
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.adapter.ArticleAdapter
import com.example.william.my.module.arch.mvvm.viewmodel.ArticleLiveDataViewModel

/**
 * Model-View-ViewModel
 * 通过 ViewModel 将数据（Model）和 UI（View）隔离，再通过 LiveData 将数据和 UI 绑定，实现数据驱动 UI。
 */
class MvvmFragment : BaseRecyclerFragment<ArticleDetailData>() {

    private val mViewModel: ArticleLiveDataViewModel by viewModels {
        ArticleLiveDataViewModel.Factory
    }

    override fun initRecyclerAdapter(): BaseQuickAdapter<ArticleDetailData, QuickViewHolder> = ArticleAdapter(arrayListOf())

    override fun observeViewModel() {
        mViewModel.articleResponse.observe(viewLifecycleOwner) { response ->
            when {
                response.code == RetrofitResponse.LOADING -> {
                    // 加载中状态
                }

                response.isSuccess -> {
                    onDataSuccess(response.data?.datas ?: emptyList())
                }

                else -> {
                    showToast(response.message.ifEmpty { "加载失败" })
                    onDataFail()
                }
            }
        }

        queryData()
    }

    override fun queryData() {
        super.queryData()
        mViewModel.loadArticle(mPage)
    }
}
