package com.example.william.my.module.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.widget.cache.RecyclerCacheExtension
import com.example.william.my.module.widget.databinding.UiItemRecyclerViewBinding

/**
 * 缓存列表适配器
 *
 * 演示 RecyclerView 缓存策略的适配器。
 */
class RecyclerCacheAdapter(private val data: List<String>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mCaches: RecyclerCacheExtension = RecyclerCacheExtension()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UiItemRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mCaches.addCache(position, holder.itemView)

        val binding = (holder as ViewHolder).binding
        binding.itemTextView.text = data?.getOrNull(position) ?: ""
    }

    override fun getItemCount(): Int = data?.size ?: 0

    /**
     * 列表项 ViewHolder
     */
    class ViewHolder(val binding: UiItemRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)
}
