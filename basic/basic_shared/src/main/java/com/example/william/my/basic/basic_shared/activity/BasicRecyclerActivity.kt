package com.example.william.my.basic.basic_shared.activity

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerRecyclerBinding

/**
 * 列表/数据展示类示例 Activity 基类。
 *
 * 布局结构：
 * - 上方展示：ConstraintLayout 容器（[mContainer] / [mBinding.basicsResponseContainer]），高度为 0dp 自适应撑满，内部包含数据展示列表（[mDataRecycler] / [mBinding.basicsDataRecycler]）
 * - 下方列表：RecyclerView 操作列表（[mRecycler] / [mBinding.basicsRecycler]），固定高度为 300dp（通过 [buildList] 与 [onRecyclerClick] 触发操作）
 *
 * 约定与规范：
 * 1. 继承类通过 [mDataRecycler] 或 [setAdapter] 配置上方区域的数据展示列表。
 * 2. 下方统一由 [buildList] + [onRecyclerClick] 触发操作。
 * 3. 适用场景：RecyclerView 各种 LayoutManager 演示、DiffUtil 局部刷新、ConcatAdapter 组合等。
 */
abstract class BasicRecyclerActivity : BasicControlActivity() {

    protected lateinit var mBinding: SharedLayoutRecyclerRecyclerBinding
    protected lateinit var mContainer: ConstraintLayout
    protected lateinit var mDataRecycler: RecyclerView

    override fun initViewBinding() {
        mBinding = SharedLayoutRecyclerRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
        mContainer = mBinding.basicsResponseContainer
        mDataRecycler = mBinding.basicsDataRecycler
    }

    /**
     * 便捷设置上方数据列表的适配器与布局管理器
     */
    protected fun setAdapter(
        adapter: RecyclerView.Adapter<*>,
        layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this),
    ) {
        mDataRecycler.layoutManager = layoutManager
        mDataRecycler.adapter = adapter
    }
}
