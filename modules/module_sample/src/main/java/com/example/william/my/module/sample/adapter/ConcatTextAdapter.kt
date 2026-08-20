package com.example.william.my.module.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.sample.R

/**
 * ConcatAdapter 子模块 1：头部 Header 模块
 */
class ConcatHeaderAdapter(
    var title: String = "【Header 模块】首页动态推荐",
    var subtitle: String = "由 HeaderAdapter 独立驱动（ViewType = 0，已配置隔离）"
) : RecyclerView.Adapter<ConcatHeaderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.sample_concat_header_title)
        val subtitleView: TextView = view.findViewById(R.id.sample_concat_header_subtitle)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_concat_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = title
        holder.subtitleView.text = subtitle
    }

    override fun getItemCount(): Int = 1
}

/**
 * ConcatAdapter 子模块 2：活动 Banner 模块（支持动态插拔）
 */
class ConcatBannerAdapter(
    var bannerTitle: String = "🎉【Banner 模块】年中狂欢大促（支持动态插拔）",
    var bannerDesc: String = "由 BannerAdapter 驱动，点击顶部【下架/上线 Banner】可动态移除或重新插入"
) : RecyclerView.Adapter<ConcatBannerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.sample_concat_banner_title)
        val descView: TextView = view.findViewById(R.id.sample_concat_banner_desc)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_concat_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = bannerTitle
        holder.descView.text = bannerDesc
    }

    override fun getItemCount(): Int = 1
}

/**
 * ConcatAdapter 子模块 3：信息流 Feed 模块
 */
class ConcatFeedAdapter(
    val items: MutableList<Pair<String, String>>
) : RecyclerView.Adapter<ConcatFeedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.sample_concat_feed_title)
        val descView: TextView = view.findViewById(R.id.sample_concat_feed_desc)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_concat_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleView.text = item.first
        holder.descView.text = item.second
    }

    override fun getItemCount(): Int = items.size
}

/**
 * ConcatAdapter 子模块 4：底部 Footer 模块
 */
class ConcatFooterAdapter(
    var footerText: String = "— 【Footer 模块】已到达页面底部（FooterAdapter） —"
) : RecyclerView.Adapter<ConcatFooterAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerTextView: TextView = view.findViewById(R.id.sample_concat_footer_text)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_concat_footer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.footerTextView.text = footerText
    }

    override fun getItemCount(): Int = 1
}
