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
 * ConcatAdapter 模块化列表组合与视图类型隔离示例
 *
 * 本示例演示 ConcatAdapter 在多模块复合列表中的标准用法与最佳实践：
 * 1. 模块化解耦：替代单个包含数十种 ViewType 的庞大 Adapter，将 Header、Banner、Feed、Footer 拆分为单一职责的子 Adapter。
 * 2. 视图类型隔离（isolateViewTypes = true）：各子 Adapter 可自由定义独立的 ViewType（如均为 0、1），由 ConcatAdapter 内部自动进行 ID 映射隔离，杜绝类型冲突。
 * 3. 独立增量刷新：子 Adapter 调用自身的 notifyItemChanged / notifyItemInserted 时，只在其所属的区间内精准局部刷新。
 * 4. 动态插拔模块：支持在运行时使用 `addAdapter(index, adapter)` / `removeAdapter(adapter)` 动态上线或下架特定业务模块。
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

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "上线/下架 Banner 模块",
            "向 Feed 列表新增项 (局部插入)",
            "局部精准更新 Feed 首条 (notifyItemChanged)",
            "上线/下架 Footer 模块"
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initSubAdapters()
        initConcatAdapter()
    }

    private fun initSubAdapters() {
        headerAdapter = ConcatHeaderAdapter()
        bannerAdapter = ConcatBannerAdapter()

        val feedList = mutableListOf(
            "推荐商品 1" to "热销爆款，限时五折优惠中",
            "推荐商品 2" to "新品首发，好评如潮，立即抢购",
            "推荐商品 3" to "爆款数码科技，会员专属特惠价"
        )
        feedAdapter = ConcatFeedAdapter(feedList)
        feedItemIndex = feedList.size + 1

        footerAdapter = ConcatFooterAdapter()
    }

    private fun initConcatAdapter() {
        // 配置 ConcatAdapter：开启 ViewType 隔离（核心保护）
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()

        concatAdapter = ConcatAdapter(
            config,
            headerAdapter,
            bannerAdapter,
            feedAdapter,
            footerAdapter
        )

        mDataRecycler.apply {
            layoutManager = LinearLayoutManager(this@ConcatAdapterActivity)
            adapter = concatAdapter
        }
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                // 1. 动态插拔 Banner 模块
                if (isBannerVisible) {
                    concatAdapter.removeAdapter(bannerAdapter)
                    isBannerVisible = false
                } else {
                    concatAdapter.addAdapter(1, bannerAdapter)
                    isBannerVisible = true
                }
            }

            1 -> {
                // 2. 向 Feed 列表新增项（触发 Feed 局部增量刷新）
                val newTitle = "新增商品 $feedItemIndex"
                val newDesc = "动态插入推荐，触发 FeedAdapter 局部 notifyItemInserted"
                feedItemIndex++
                feedAdapter.items.add(newTitle to newDesc)
                feedAdapter.notifyItemInserted(feedAdapter.items.size - 1)
            }

            2 -> {
                // 3. 仅更新 Feed 首条（验证局部精准刷新）
                if (feedAdapter.items.isNotEmpty()) {
                    val old = feedAdapter.items[0]
                    feedAdapter.items[0] = old.first to "已局部精准刷新：${System.currentTimeMillis() % 1000}"
                    feedAdapter.notifyItemChanged(0)
                }
            }

            3 -> {
                // 4. 动态插拔 Footer 模块
                if (isFooterVisible) {
                    concatAdapter.removeAdapter(footerAdapter)
                    isFooterVisible = false
                } else {
                    concatAdapter.addAdapter(concatAdapter.adapters.size, footerAdapter)
                    isFooterVisible = true
                }
            }
        }
    }
}
