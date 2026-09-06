package com.example.william.my.module.widget.cache

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 缓存扩展
 *
 * 实现 ViewCacheExtension 自定义缓存。
 */
class RecyclerCacheExtension : RecyclerView.ViewCacheExtension() {

    private val mViewCache: SparseArray<View> = SparseArray(4)

    override fun getViewForPositionAndType(
        recycler: RecyclerView.Recycler,
        position: Int,
        type: Int,
    ): View? = if (mViewCache.size() > position) {
        mViewCache[position]
    } else {
        null
    }

    fun addCache(position: Int, view: View) {
        if (mViewCache[position] !== view) {
            mViewCache.put(position, view)
        }
    }

    fun clearCache() {
        mViewCache.clear()
    }
}
