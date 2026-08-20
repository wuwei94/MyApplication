package com.example.william.my.module.sample.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
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
                if (payload is Bundle && payload.containsKey(PAYLOAD_KEY_LIKES)) {
                    val newLikes = payload.getInt(PAYLOAD_KEY_LIKES)
                    holder.desc.text = "👍 点赞数：$newLikes（✨ Payload 局部刷新，无整项闪烁）"
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    /**
     * 内置 DiffUtil.Callback 差量比对计算器
     */
    class DiffCallback(
        private val oldList: List<ArticleItem>,
        private val newList: List<ArticleItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        // 步骤 1：比较是否为同一个数据项（通常比较唯一主键）
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        // 步骤 2：比较内容是否完全一致（若一致则无需任何重绘）
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        // 步骤 3：可选，针对局部字段变化提供 Payload 标记
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            val payload = Bundle()
            if (oldItem.likes != newItem.likes) {
                payload.putInt(PAYLOAD_KEY_LIKES, newItem.likes)
            }
            return if (payload.isEmpty) null else payload
        }
    }

    companion object {
        const val PAYLOAD_KEY_LIKES = "payload_key_likes"
    }
}
