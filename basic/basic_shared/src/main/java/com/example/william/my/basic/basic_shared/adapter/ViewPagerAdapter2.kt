package com.example.william.my.basic.basic_shared.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseBinding

/**
 * ViewPager2 的 RecyclerView.Adapter
 *
 * ViewPager2 基于 RecyclerView 实现，所以需要提供 RecyclerView.Adapter
 * 适合需要 RecyclerView 特性（如 ViewHolder 复用）的场景
 *
 * @param mData 页面数据列表
 */
class ViewPagerAdapter2(private val mData: List<String> = emptyList()) : RecyclerView.Adapter<ViewPagerAdapter2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SharedLayoutResponseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.basicsResponse.text = mData.getOrNull(position) ?: ""
    }

    override fun getItemCount(): Int = mData.size

    class ViewHolder(val binding: SharedLayoutResponseBinding) : RecyclerView.ViewHolder(binding.root)
}
