package com.example.william.my.basic.basic_shared.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseBinding

/**
 * ViewPager 的 PagerAdapter
 *
 * PagerAdapter: 需要手动管理 View 的创建和销毁
 * 适合简单的页面切换场景
 *
 * @param mData 页面数据列表
 */
class ViewPagerAdapter(private val mData: List<String> = emptyList()) : PagerAdapter() {

    override fun getCount(): Int = mData.size

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = SharedLayoutResponseBinding.inflate(
            LayoutInflater.from(container.context), container, true
        )
        binding.basicsResponse.text = mData.getOrNull(position) ?: ""
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
