package com.example.william.my.module.jetpack.paging.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.module.jetpack.databinding.JetpackItemRecyclerBinding

/**
 * Paging RecyclerView 适配器
 */
class PagingAdapter(diffCallback: DiffUtil.ItemCallback<ArticleDetailData>) : PagingDataAdapter<ArticleDetailData, PagingAdapter.ViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = position.toString() + ". " + getItem(position)?.title
        holder.binding.itemTextView.text = item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bind =
            JetpackItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }

    /**
     * 分页列表项 ViewHolder
     */
    class ViewHolder(bind: JetpackItemRecyclerBinding) : RecyclerView.ViewHolder(bind.root) {
        var binding: JetpackItemRecyclerBinding = bind
    }

    /**
     * 分页差异比较器
     *
     * 比较文章条目是否相同。
     */
    class PagingComparator : DiffUtil.ItemCallback<ArticleDetailData>() {

        override fun areItemsTheSame(
            oldItem: ArticleDetailData,
            newItem: ArticleDetailData,
        ): Boolean {
            // id 具有唯一性。
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ArticleDetailData,
            newItem: ArticleDetailData,
        ): Boolean = oldItem == newItem
    }
}
