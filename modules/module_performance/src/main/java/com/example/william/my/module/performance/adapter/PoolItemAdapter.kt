package com.example.william.my.module.performance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.R

/**
 * RecycledViewPool 演示用列表 Adapter
 *
 * 通过统一的 VIEW_TYPE 实现跨列表复用，并在 onCreate / onBind 时回调通知统计。
 */
class PoolItemAdapter(
    val tabName: String,
    val items: List<String>,
    private val onCreateCallback: () -> Unit,
    private val onBindCallback: () -> Unit,
) : RecyclerView.Adapter<PoolItemAdapter.ViewHolder>() {

    /**
     * 池列表项 ViewHolder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.performance_pool_item_title)
        val descView: TextView = view.findViewById(R.id.performance_pool_item_desc)
    }

    override fun getItemViewType(position: Int): Int = VIEW_TYPE_CARD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        onCreateCallback.invoke()
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.performance_item_pool_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindCallback.invoke()
        val item = items[position]
        holder.titleView.text = "[$tabName] $item"
        holder.descView.text = "ViewHolder Hash: #${System.identityHashCode(holder)}"
    }

    override fun getItemCount(): Int = items.size

    companion object {
        const val VIEW_TYPE_CARD = 100
    }
}
