package com.example.william.my.module.performance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.R

/**
 * ConcatAdapter 子模块 4：底部 Footer 模块
 */
class ConcatFooterAdapter(
    var footerText: String = "— 【Footer 模块】已到达页面底部（FooterAdapter） —",
) : RecyclerView.Adapter<ConcatFooterAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerTextView: TextView = view.findViewById(R.id.performance_concat_footer_text)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.performance_item_concat_footer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.footerTextView.text = footerText
    }

    override fun getItemCount(): Int = 1
}
