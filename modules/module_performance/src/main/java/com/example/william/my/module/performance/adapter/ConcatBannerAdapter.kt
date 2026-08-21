package com.example.william.my.module.performance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.R

/**
 * ConcatAdapter 子模块 2：活动 Banner 模块（支持动态插拔）
 */
class ConcatBannerAdapter(
    var bannerTitle: String = "🎉【Banner 模块】年中狂欢大促（支持动态插拔）",
    var bannerDesc: String = "由 BannerAdapter 驱动，点击顶部【下架/上线 Banner】可动态移除或重新插入"
) : RecyclerView.Adapter<ConcatBannerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.performance_concat_banner_title)
        val descView: TextView = view.findViewById(R.id.performance_concat_banner_desc)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.performance_item_concat_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = bannerTitle
        holder.descView.text = bannerDesc
    }

    override fun getItemCount(): Int = 1
}
