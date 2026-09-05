package com.example.william.my.module.performance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.R

/**
 * ConcatAdapter 子模块 1：头部 Header 模块
 */
class ConcatHeaderAdapter(
    var title: String = "【Header 模块】首页动态推荐",
    var subtitle: String = "由 HeaderAdapter 独立驱动（ViewType = 0，已配置隔离）",
) : RecyclerView.Adapter<ConcatHeaderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.performance_concat_header_title)
        val subtitleView: TextView = view.findViewById(R.id.performance_concat_header_subtitle)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.performance_item_concat_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = title
        holder.subtitleView.text = subtitle
    }

    override fun getItemCount(): Int = 1
}
