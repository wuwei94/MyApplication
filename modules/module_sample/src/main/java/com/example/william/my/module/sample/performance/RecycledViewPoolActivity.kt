package com.example.william.my.module.sample.performance

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.sample.adapter.PoolItemAdapter
import com.example.william.my.module.sample.databinding.SampleActivityRecycledViewPoolBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * RecyclerView.RecycledViewPool 跨列表/Tab 共享视图池示例
 *
 * 本示例演示 RecycledViewPool 在多列表场景下的真实渲染与最佳实践：
 * 1. 核心机制：RecyclerView 默认各自拥有独立的 RecycledViewPool。在 ViewPager2 多 Tab 或垂直列表嵌套横向列表场景中，
 *    各列表若拥有相同的 Item 样式，可通过共享同一个 RecycledViewPool，实现 ViewHolder 跨列表直接复用。
 * 2. 性能收益：大幅减少滑动与切页时的 `onCreateViewHolder` 次数与内存开销，彻底消除切 Tab 时的创建视图卡顿。
 * 3. setMaxRecycledViews(viewType, maxCount)：针对高频 Item 类型调大缓存容量（默认 5）。
 * 4. 生命周期管理：列表退出或数据源销毁时适时调用 `clear()` 清空池内引用。
 */
@Route(path = RouterPath.Sample.RecycledViewPool)
class RecycledViewPoolActivity : BaseVBActivity<SampleActivityRecycledViewPoolBinding>() {

    // 创建页面级共享的 RecycledViewPool
    private val sharedViewPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(PoolItemAdapter.VIEW_TYPE_CARD, 15)
    }

    private var tab1CreateCount = 0
    private var tab1BindCount = 0
    private var tab2CreateCount = 0
    private var tab2BindCount = 0

    override fun getViewBinding(): SampleActivityRecycledViewPoolBinding {
        return SampleActivityRecycledViewPoolBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initViewPager()
        initActionButtons()
        updateHudStats()
    }

    private fun initViewPager() {
        val tabTitles = listOf("推荐专区 (Tab 1)", "热门专区 (Tab 2)")

        // 使用 ViewPager2 承载两个共用池的 RecyclerView
        mBinding.samplePoolViewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabTitles.size

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val recyclerView = RecyclerView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    layoutManager = LinearLayoutManager(parent.context)
                    clipToPadding = false
                    setPadding(0, 0, 0, 32)
                    // 核心关键：为每个 Tab 的 RecyclerView 设置同一个 RecycledViewPool
                    setRecycledViewPool(sharedViewPool)
                }
                return object : RecyclerView.ViewHolder(recyclerView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val recyclerView = holder.itemView as RecyclerView
                val isTab1 = position == 0
                val data = (1..60).map { "商品卡片 #$it" }

                val adapter = PoolItemAdapter(
                    tabName = if (isTab1) "Tab 1" else "Tab 2",
                    items = data,
                    onCreateCallback = {
                        if (isTab1) tab1CreateCount++ else tab2CreateCount++
                        updateHudStats()
                    },
                    onBindCallback = {
                        if (isTab1) tab1BindCount++ else tab2BindCount++
                        updateHudStats()
                    }
                )
                recyclerView.adapter = adapter
            }
        }

        // TabLayout 与 ViewPager2 联动
        TabLayoutMediator(
            mBinding.samplePoolTabLayout,
            mBinding.samplePoolViewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun initActionButtons() {
        mBinding.sampleBtnClearPool.setOnClickListener {
            sharedViewPool.clear()
            updateHudStats()
        }

        mBinding.sampleBtnResetStats.setOnClickListener {
            tab1CreateCount = 0
            tab1BindCount = 0
            tab2CreateCount = 0
            tab2BindCount = 0
            updateHudStats()
        }
    }

    private fun updateHudStats() {
        runOnUiThread {
            val poolCount = sharedViewPool.getRecycledViewCount(PoolItemAdapter.VIEW_TYPE_CARD)
            val tab2ReuseCount = (tab2BindCount - tab2CreateCount).coerceAtLeast(0)

            mBinding.samplePoolStatsText.text =
                "共享池当前缓存数: $poolCount (上限: 15)\n" +
                        "Tab 1 (推荐): 创建 $tab1CreateCount 次 | 绑定 $tab1BindCount 次\n" +
                        "Tab 2 (热门): 创建 $tab2CreateCount 次 | 绑定 $tab2BindCount 次 (跨池复用命中: $tab2ReuseCount 次)"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewPool.clear()
    }
}
