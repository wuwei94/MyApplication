package com.example.william.my.module.jetpack.paging.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.jetpack.R
import com.example.william.my.module.jetpack.databinding.JetpackItemRecyclerBinding

/**
 * 分页加载状态适配器
 *
 * 展示分页加载、错误等页脚状态。
 */
class PagingStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<PagingStateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.mBinding.itemTextView.setOnClickListener {
            if (loadState is LoadState.Error) {
                retry()
            }
        }
        when (loadState) {
            is LoadState.Loading -> {
                holder.mBinding.itemTextView.text = "正在加载更多数据..."
                holder.mBinding.itemTextView.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.shared_color_primary,
                    ),
                )
            }

            is LoadState.NotLoading -> {
                holder.mBinding.itemTextView.text = ""
                holder.mBinding.itemTextView.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        android.R.color.transparent,
                    ),
                )
            }

            is LoadState.Error -> {
                holder.mBinding.itemTextView.text = "加载失败，点击重试 (${loadState.error.localizedMessage ?: "网络异常"})"
                holder.mBinding.itemTextView.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.shared_color_primary_dark,
                    ),
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val bind =
            JetpackItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }

    /**
     * 加载状态 ViewHolder
     */
    class ViewHolder(bind: JetpackItemRecyclerBinding) : RecyclerView.ViewHolder(bind.root) {
        var mBinding: JetpackItemRecyclerBinding = bind
    }
}
