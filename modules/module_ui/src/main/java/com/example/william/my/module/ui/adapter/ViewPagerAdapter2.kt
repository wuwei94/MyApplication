package com.example.william.my.module.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.ui.databinding.UiPageViewPagerBinding

class ViewPagerAdapter2(private val mData: List<String>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            UiPageViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        mData?.let { data ->
            binding.response.text = data[position]
        }
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    class ViewHolder(val binding: UiPageViewPagerBinding) :
        RecyclerView.ViewHolder(binding.root)

}