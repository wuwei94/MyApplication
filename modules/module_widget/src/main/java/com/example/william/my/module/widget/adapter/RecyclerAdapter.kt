package com.example.william.my.module.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.widget.databinding.UiItemRecyclerViewBinding

class RecyclerAdapter(private val data: List<String>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UiItemRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        binding.itemTextView.text = data?.getOrNull(position) ?: ""
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            //会执行不带payloads参数的onBindViewHolder
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val payload = payloads[0] as String
            (holder as ViewHolder).binding.itemTextView.text = payload
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    class ViewHolder(val binding: UiItemRecyclerViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}