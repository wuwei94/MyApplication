package com.example.william.my.module.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.widget.databinding.UiItemRecyclerViewBinding
import com.example.william.my.module.widget.databinding.UiItemRecyclerViewNestedBinding

/**
 * RecyclerView 嵌套 RecyclerView
 */
class RecyclerNestedAdapter(private var data: List<String>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UiItemRecyclerViewNestedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        binding.itemRecycleView.layoutManager = LinearLayoutManager(holder.itemView.context)
        binding.itemRecycleView.adapter = RecyclerAdapter(data)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class ViewHolder(val binding: UiItemRecyclerViewNestedBinding) : RecyclerView.ViewHolder(binding.root)

    class RecyclerAdapter(private val mData: List<String>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = UiItemRecyclerViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = (holder as ViewHolder).binding
            binding.itemTextView.text = mData?.getOrNull(position) ?: ""
        }

        override fun getItemCount(): Int = mData?.size ?: 0

        class ViewHolder(val binding: UiItemRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
