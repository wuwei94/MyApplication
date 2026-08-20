package com.example.william.my.module.sample.performance

import android.os.Bundle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.sample.adapter.ConcatBannerAdapter
import com.example.william.my.module.sample.adapter.ConcatFeedAdapter
import com.example.william.my.module.sample.adapter.ConcatFooterAdapter
import com.example.william.my.module.sample.adapter.ConcatHeaderAdapter
import com.example.william.my.module.sample.databinding.SampleActivityConcatBinding

/**
 * ConcatAdapter 模块化列表组合与视图类型隔离示例
 *
 * 本示例演示 ConcatAdapter 在多模块复合列表中的标准用法与最佳实践：
 * 1. 模块化解耦：替代单个包含数十种 ViewType 的庞大 Adapter，将 Header、Banner、Feed、Footer 拆分为单一职责的子 Adapter。
 * 2. 视图类型隔离（isolateViewTypes = true）：各子 Adapter 可自由定义独立的 ViewType（如均为 0、1），由 ConcatAdapter 内部自动进行 ID 映射隔离，杜绝类型冲突。
 * 3. 独立增量刷新：子 Adapter 调用自身的 notifyItemChanged / notifyItemInserted 时，只在其所属的区间内精准局部刷新。
 * 4. 动态插拔模块：支持在运行时使用 `addAdapter(index, adapter)` / `removeAdapter(adapter)` 动态上线或下架特定业务模块。
 */
@Route(path = RouterPath.Sample.ConcatAdapter)
class ConcatAdapterActivity : BaseVBActivity<SampleActivityConcatBinding>() {

    private lateinit var headerAdapter: ConcatHeaderAdapter
    private lateinit var bannerAdapter: ConcatBannerAdapter
    private lateinit var feedAdapter: ConcatFeedAdapter
    private lateinit var footerAdapter: ConcatFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter

    private var isBannerActive = true
    private var isFooterActive = true
    private var feedItemCounter = 103

    override fun getViewBinding(): SampleActivityConcatBinding {
        return SampleActivityConcatBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initAdapters()
        initRecyclerView()
        initActionButtons()
        updateStatusInfo()
    }

    private fun initAdapters() {
        // 1. 初始化各子模块 Adapter（各自内部 ViewType 均定义为 0）
        headerAdapter = ConcatHeaderAdapter()
        bannerAdapter = ConcatBannerAdapter()

        val feedItems = mutableListOf(
            "商品 Item #101" to "热销商品，库存充足，点击查看详情",
            "商品 Item #102" to "秒杀特惠，限时抢购中",
            "商品 Item #103" to "新品首发，专享早鸟优惠"
        )
        feedAdapter = ConcatFeedAdapter(feedItems)
        footerAdapter = ConcatFooterAdapter()

        // 2. 使用 ConcatAdapter.Config 配置视图类型隔离
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true) // 关键：隔离各子 Adapter 的 viewType，避免冲突
            .build()

        // 3. 组装多模块 ConcatAdapter
        concatAdapter = ConcatAdapter(config, headerAdapter, bannerAdapter, feedAdapter, footerAdapter)
    }

    private fun initRecyclerView() {
        mBinding.sampleConcatRecycler.apply {
            layoutManager = LinearLayoutManager(this@ConcatAdapterActivity)
            adapter = concatAdapter
        }
    }

    private fun initActionButtons() {
        // 1. 动态下架 / 恢复 Banner 模块
        mBinding.sampleBtnToggleBanner.setOnClickListener {
            if (isBannerActive) {
                // 动态移除 Banner
                concatAdapter.removeAdapter(bannerAdapter)
                isBannerActive = false
                mBinding.sampleBtnToggleBanner.text = "重新上线 Banner 模块"
            } else {
                // 动态重新插入 Banner 至 Header 下方（索引 1）
                concatAdapter.addAdapter(1, bannerAdapter)
                isBannerActive = true
                mBinding.sampleBtnToggleBanner.text = "下架 Banner 模块"
            }
            updateStatusInfo()
        }

        // 2. Feed 模块独立新增条目（仅刷新 Feed 区域）
        mBinding.sampleBtnInsertFeed.setOnClickListener {
            feedItemCounter++
            feedAdapter.items.add("商品 Item #$feedItemCounter" to "由 Feed 模块独立新增，未触发整表重绑")
            feedAdapter.notifyItemInserted(feedAdapter.items.size - 1)
            updateStatusInfo()
        }

        // 3. Feed 模块仅刷新首条
        mBinding.sampleBtnUpdateFeed.setOnClickListener {
            if (feedAdapter.items.isNotEmpty()) {
                val first = feedAdapter.items[0]
                feedAdapter.items[0] = first.first to "首条数据已更新（${System.currentTimeMillis() % 10000}）"
                feedAdapter.notifyItemChanged(0)
            }
        }

        // 4. 追加 / 移除 Footer 模块
        mBinding.sampleBtnToggleFooter.setOnClickListener {
            if (isFooterActive) {
                concatAdapter.removeAdapter(footerAdapter)
                isFooterActive = false
                mBinding.sampleBtnToggleFooter.text = "追加 Footer 模块"
            } else {
                concatAdapter.addAdapter(footerAdapter)
                isFooterActive = true
                mBinding.sampleBtnToggleFooter.text = "移除 Footer 模块"
            }
            updateStatusInfo()
        }
    }

    private fun updateStatusInfo() {
        val adapterCount = concatAdapter.adapters.size
        val totalItemCount = concatAdapter.itemCount
        mBinding.sampleConcatStatus.text =
            "子 Adapter: $adapterCount 个 | 全局合并项: $totalItemCount 项 | isolateViewTypes: true"
    }
}
