package com.example.william.my.module.sample.activity.recycler

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.sample.adapter.PoolItemAdapter
import com.example.william.my.module.sample.adapter.PoolPagerAdapter
import com.example.william.my.module.sample.databinding.SampleLayoutPoolPagerBinding
import com.example.william.my.module.sample.pool.GlobalRecycledViewPool
import com.google.android.material.tabs.TabLayoutMediator

/**
 * RecyclerView.RecycledViewPool 跨列表/Tab 共享视图池示例
 *
 * 本示例演示 RecycledViewPool 在多列表/跨页面场景下的真实渲染与 GlobalRecycledViewPool 全局管理：
 * 1. 核心机制：RecyclerView 默认各自拥有独立的 RecycledViewPool。在 ViewPager2 多 Tab 或多页面跳转场景中，
 *    各列表若拥有相同的 Item 样式，可通过共享 GlobalRecycledViewPool 单例，实现 ViewHolder 跨列表直接复用。
 * 2. 性能收益：大幅减少滑动与切页时的 `onCreateViewHolder` 次数与内存开销，彻底消除切 Tab 时的创建视图卡顿。
 * 3. setMaxRecycledViews(viewType, maxCount)：针对高频 Item 类型调大缓存容量（默认 5，本例调至 15）。
 * 4. 生命周期管理：列表退出或数据源销毁时适时调用 `GlobalRecycledViewPool.clear()` 清空池内引用。
 */
@Route(path = RouterPath.Sample.RecycledViewPool)
class RecycledViewPoolActivity : BasicRecyclerActivity() {

    private lateinit var poolBinding: SampleLayoutPoolPagerBinding

    private var tab1CreateCount = 0
    private var tab1BindCount = 0
    private var tab2CreateCount = 0
    private var tab2BindCount = 0

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "清空全局共享池 (GlobalRecycledViewPool.clear())",
            "重置创建/绑定统计计数",
            "查看当前池状态及统计"
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 配置全局复用池对卡片类型的最大容量
        GlobalRecycledViewPool.setMaxRecycledViews(PoolItemAdapter.VIEW_TYPE_CARD, 15)

        initViewPager()
    }

    private fun initViewPager() {
        mContainer.removeAllViews()
        poolBinding = SampleLayoutPoolPagerBinding.inflate(layoutInflater, mContainer, true)

        val tabTitles = listOf("推荐专区 (Tab 1)", "热门专区 (Tab 2)")

        // 统一使用全局共享的 GlobalRecycledViewPool
        poolBinding.samplePoolViewPager.adapter = PoolPagerAdapter(
            sharedViewPool = GlobalRecycledViewPool.getPool(),
            tabCount = tabTitles.size,
            onCreateItem = { isTab1 ->
                if (isTab1) tab1CreateCount++ else tab2CreateCount++
                logHudStats()
            },
            onBindItem = { isTab1 ->
                if (isTab1) tab1BindCount++ else tab2BindCount++
                logHudStats()
            }
        )

        // TabLayout 与 ViewPager2 联动
        TabLayoutMediator(
            poolBinding.samplePoolTabLayout,
            poolBinding.samplePoolViewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                GlobalRecycledViewPool.clear()
                logHudStats("已清空共享池")
            }

            1 -> {
                tab1CreateCount = 0
                tab1BindCount = 0
                tab2CreateCount = 0
                tab2BindCount = 0
                logHudStats("已重置统计计数")
            }

            2 -> {
                logHudStats()
            }
        }
    }

    private fun logHudStats(prefix: String = "") {
        val poolCount = GlobalRecycledViewPool.getRecycledViewCount(PoolItemAdapter.VIEW_TYPE_CARD)
        val tab2ReuseCount = (tab2BindCount - tab2CreateCount).coerceAtLeast(0)
        val message = (if (prefix.isNotEmpty()) "$prefix | " else "") +
                "池缓存数: $poolCount (上限: 15) | Tab 1 创建: $tab1CreateCount, 绑定: $tab1BindCount | Tab 2 创建: $tab2CreateCount, 绑定: $tab2BindCount (跨池复用: $tab2ReuseCount)"
        Utils.logcat("RecycledViewPool", message)
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalRecycledViewPool.clear()
    }
}
