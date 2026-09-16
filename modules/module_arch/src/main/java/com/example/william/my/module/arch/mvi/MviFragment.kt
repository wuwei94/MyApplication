package com.example.william.my.module.arch.mvi

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.base.ui.recycler.BaseRecyclerFragment
import com.example.william.my.module.arch.adapter.ArticleAdapter
import com.example.william.my.module.arch.mvi.data.ArticleIntent
import com.example.william.my.module.arch.mvi.data.ArticleUiEffect
import com.example.william.my.module.arch.mvi.data.ArticleUiState
import com.example.william.my.module.arch.mvi.viewmodel.ArticleStateFlowViewModel
import kotlinx.coroutines.launch

/**
 * MVI：Model-View-Intent
 * 1. 将 LiveData 组件改成了 StateFlow
 * 2. ViewModel 传递给 View 的数据限制为 View 的 UIState
 * 3. 单次事件（如 Toast/导航）通过 Effect 通道分发，避免重放
 */
class MviFragment : BaseRecyclerFragment<ArticleDetailData>() {

    private val viewModel: ArticleStateFlowViewModel by viewModels {
        ArticleStateFlowViewModel.Factory
    }

    override fun initRecyclerAdapter(): BaseQuickAdapter<ArticleDetailData, QuickViewHolder> = ArticleAdapter(arrayListOf())

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 监听 UI 状态
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ArticleUiState.Loading -> {
                                // 加载中状态
                            }

                            is ArticleUiState.Success -> {
                                onDataSuccess(state.articles)
                            }

                            is ArticleUiState.Error -> {
                                onDataFail()
                            }
                        }
                    }
                }

                // 监听单次副作用事件（如 Toast）
                launch {
                    viewModel.uiEffect.collect { effect ->
                        when (effect) {
                            is ArticleUiEffect.ShowToast -> {
                                showToast(effect.message)
                            }
                        }
                    }
                }
            }
        }

        queryData()
    }

    override fun queryData() {
        super.queryData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.intent.send(ArticleIntent.LoadArticleIntent(page))
        }
    }
}
