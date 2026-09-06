package com.example.william.my.module.performance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.R

/**
 * ConcatAdapter 子模块 3：信息流 Feed 模块
 */
class ConcatFeedAdapter(
    val items: MutableList<Pair<String, String>>,
) : RecyclerView.Adapter<ConcatFeedAdapter.ViewHolder>() {

    /**
     * Feed 列表项 ViewHolder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.performance_concat_feed_title)
        val descView: TextView = view.findViewById(R.id.performance_concat_feed_desc)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.performance_item_concat_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleView.text = item.first
        holder.descView.text = item.second
    }

    override fun getItemCount(): Int = items.size
}
