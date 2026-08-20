package com.example.william.my.module.sample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.sample.R
import com.example.william.my.module.sample.bean.ArticleItem

/**
 * 支持 DiffUtil Payload 细粒度刷新的 RecyclerView.Adapter 示例
 */
class ArticleAdapter(
    var dataList: MutableList<ArticleItem>,
    private val onItemClick: ((ArticleItem) -> Unit)? = null
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.sample_diff_title)
        val desc: TextView = view.findViewById(R.id.sample_diff_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_diff, parent, false)
        return ViewHolder(view)
    }

    // 全量绑定（整项重新绑定）
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.title.text = "[ID: ${item.id}] ${item.title}"
        holder.desc.text = "👍 点赞数：${item.likes}（整项全量重绑：${System.currentTimeMillis() % 10000}）"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    // Payload 局部精准绑定（避免整项重新绑定闪烁）
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                if (payload is Bundle && payload.containsKey(ArticleDiffCallback.PAYLOAD_KEY_LIKES)) {
                    val newLikes = payload.getInt(ArticleDiffCallback.PAYLOAD_KEY_LIKES)
                    holder.desc.text = "👍 点赞数：$newLikes（✨ Payload 局部刷新，无整项闪烁）"
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
