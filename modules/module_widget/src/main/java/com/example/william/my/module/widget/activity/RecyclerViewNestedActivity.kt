package com.example.william.my.module.widget.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget.adapter.RecyclerNestedAdapter
import com.example.william.my.module.widget.databinding.UiActivityRecyclerViewNestedBinding

/**
 * RecyclerView 嵌套 — 嵌套滚动示例
 *
 * 演示 RecyclerView 嵌套使用，解决嵌套滚动冲突。
 *
 * 核心特性：
 * 1. 嵌套滚动：外层 RecyclerView 每个 item 内包含一个内层 RecyclerView
 * 2. 冲突解决：使用自定义 NestedRecyclerView 解决嵌套滚动冲突
 * 3. 性能优化：合理使用 setHasFixedSize(true) 提升性能
 * 4. 布局灵活：支持多种嵌套布局方式
 *
 * 解决方案：
 * 1. 自定义 NestedRecyclerView：重写 onMeasure() 方法
 * 2. 禁用内层 RecyclerView 的嵌套滚动：setNestedScrollingEnabled(false)
 * 3. 使用 NestedScrollView：作为外层容器
 *
 * 基本用法：
 * ```kotlin
 * // 自定义 NestedRecyclerView
 * class NestedRecyclerView : RecyclerView {
 *     override fun onMeasure(widthSpec: Int, heightSpec: Int) {
 *         // 解决嵌套滚动冲突
 *         val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
 *         super.onMeasure(widthSpec, expandSpec)
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 复杂列表布局
 * - 多层嵌套列表
 * - 需要嵌套滚动的场景
 */
@Route(path = RouterPath.Widget.RecyclerViewNested)
class RecyclerViewNestedActivity : BaseVBActivity<UiActivityRecyclerViewNestedBinding>() {

    override fun getViewBinding(): UiActivityRecyclerViewNestedBinding {
        return UiActivityRecyclerViewNestedBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecycleView()
    }

    private fun initRecycleView() {
        val data = (1..20).map { "POSITION $it" }

        mBinding.recycleView.layoutManager = LinearLayoutManager(this)
        mBinding.recycleView.adapter = RecyclerNestedAdapter(data)
    }
}
