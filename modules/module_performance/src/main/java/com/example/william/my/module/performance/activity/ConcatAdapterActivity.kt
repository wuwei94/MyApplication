package com.example.william.my.module.performance.activity

import android.os.Bundle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.adapter.ConcatBannerAdapter
import com.example.william.my.module.performance.adapter.ConcatFeedAdapter
import com.example.william.my.module.performance.adapter.ConcatFooterAdapter
import com.example.william.my.module.performance.adapter.ConcatHeaderAdapter

/**
 * ConcatAdapter — 模块化列表组合与视图类型隔离
 *
 * 本示例演示 ConcatAdapter 在多模块复合列表中的标准用法与最佳实践。
 *
 * 核心机制与避坑点：
 * 1. 模块化解耦：替代单个包含数十种 ViewType 的庞大 Adapter，将 Header、Banner、Feed、Footer 拆分为单一职责的子 Adapter
 * 2. 视图类型隔离（isolateViewTypes = true）：各子 Adapter 可自由定义独立的 ViewType（如均为 0、1），由 ConcatAdapter 内部自动进行 ID 映射隔离，杜绝类型冲突
 * 3. 独立增量刷新：子 Adapter 调用自身的 notifyItemChanged / notifyItemInserted 时，只在其所属的区间内精准局部刷新
 * 4. 动态插拔模块：支持在运行时使用 addAdapter(index, adapter) / removeAdapter(adapter) 动态上线或下架特定业务模块
 *
 * https://developer.android.com/reference/androidx/recyclerview/widget/ConcatAdapter
 */
@Route(path = RouterPath.Performance.ConcatAdapter)
class ConcatAdapterActivity : BasicRecyclerActivity() {

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var headerAdapter: ConcatHeaderAdapter
    private lateinit var bannerAdapter: ConcatBannerAdapter
    private lateinit var feedAdapter: ConcatFeedAdapter
    private lateinit var footerAdapter: ConcatFooterAdapter

    private var isBannerVisible = true
    private var isFooterVisible = true
    private var feedItemIndex = 1

    override fun buildList(): ArrayList<String> = arrayListOf(
        "上线/下架 Banner 模块",
        "向 Feed 列表新增项 (局部插入)",
        "局部精准更新 Feed 首条 (notifyItemChanged)",
        "上线/下架 Footer 模块",
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initConcatAdapter()
    }

    private fun initConcatAdapter() {
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()

        headerAdapter = ConcatHeaderAdapter("顶栏：今日焦点动态 (Header)")
        bannerAdapter = ConcatBannerAdapter("🔥 热点 1: 性能优化指南", "🚀 热点 2: 现代 Kotlin 架构")
        feedAdapter = ConcatFeedAdapter(
            mutableListOf(
                "信息流条目 1" to "理解 View 渲染流水线",
                "信息流条目 2" to "深入协程挂起本质",
                "信息流条目 3" to "Compose 智能重组机制",
            ),
        )
        footerAdapter = ConcatFooterAdapter("底栏：到底啦 ~ (Footer)")

        concatAdapter = ConcatAdapter(
            config,
            headerAdapter,
            bannerAdapter,
            feedAdapter,
            footerAdapter,
        )

        dataRecycler.apply {
            layoutManager = LinearLayoutManager(this@ConcatAdapterActivity)
            adapter = concatAdapter
        }
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                // 1. 动态挂载/卸载 Banner
                if (isBannerVisible) {
                    concatAdapter.removeAdapter(bannerAdapter)
                    isBannerVisible = false
                } else {
                    concatAdapter.addAdapter(1, bannerAdapter)
                    isBannerVisible = true
                }
            }

            1 -> {
                // 2. 向 Feed 局部插入
                val newItem = "动态新增流项目 #${feedItemIndex++}" to "动态局部刷新"
                val currentSize = feedAdapter.items.size
                feedAdapter.items.add(newItem)
                feedAdapter.notifyItemInserted(currentSize)
            }

            2 -> {
                // 3. 局部定向刷新首条
                if (feedAdapter.items.isNotEmpty()) {
                    val old = feedAdapter.items[0]
                    feedAdapter.items[0] = old.first to "已定向更新首条 @ ${System.currentTimeMillis() % 10000}"
                    feedAdapter.notifyItemChanged(0)
                }
            }

            3 -> {
                // 4. 动态挂载/卸载 Footer
                if (isFooterVisible) {
                    concatAdapter.removeAdapter(footerAdapter)
                    isFooterVisible = false
                } else {
                    concatAdapter.addAdapter(footerAdapter)
                    isFooterVisible = true
                }
            }
        }
    }
}
