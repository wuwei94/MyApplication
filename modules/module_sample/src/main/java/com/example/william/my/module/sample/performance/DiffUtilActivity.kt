package com.example.william.my.module.sample.performance

import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.sample.adapter.ArticleAdapter
import com.example.william.my.module.sample.bean.ArticleItem
import com.example.william.my.module.sample.databinding.SampleActivityDiffBinding

/**
 * DiffUtil 列表差量计算与局部更新示例
 *
 * 本示例演示 DiffUtil 在列表更新中的标准写法与真实渲染效果：
 * 1. areItemsTheSame(oldPos, newPos)：判断是否为同一个条目（通常比较唯一主键 ID）。
 * 2. areContentsTheSame(oldPos, newPos)：判断条目内容是否完全一致（内容未变则跳过刷新）。
 * 3. getChangePayload(oldPos, newPos)：提取变化字段的 Payload，用于极细粒度局部刷新，避免整个 ViewHolder 重新绑定。
 * 4. dispatchUpdatesTo(adapter)：将增、删、改、移精准定向分发给 Adapter，触发平滑的 ItemAnimator 动画。
 */
@Route(path = RouterPath.Sample.DiffUtil)
class DiffUtilActivity : BaseVBActivity<SampleActivityDiffBinding>() {

    private var currentList = mutableListOf<ArticleItem>()
    private lateinit var articleAdapter: ArticleAdapter

    override fun getViewBinding(): SampleActivityDiffBinding {
        return SampleActivityDiffBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initData()
        initRecyclerView()
        initActionButtons()
        updateStatusInfo()
    }

    private fun initData() {
        currentList = arrayListOf(
            ArticleItem(id = 1, title = "Kotlin 协程与 Flow 深入剖析", likes = 10),
            ArticleItem(id = 2, title = "Android 性能优化实战与内存管理", likes = 25),
            ArticleItem(id = 3, title = "Jetpack Compose 现代化 UI 指南", likes = 8),
            ArticleItem(id = 4, title = "RecyclerView 缓存与复用机制精讲", likes = 16)
        )
    }

    private fun initRecyclerView() {
        articleAdapter = ArticleAdapter(currentList.toMutableList()) { clickedItem ->
            // 点击任意 Item 点赞 +1
            val newList = currentList.map {
                if (it.id == clickedItem.id) it.copy(likes = it.likes + 1) else it.copy()
            }.toMutableList()
            applyDiffResult(newList)
        }

        mBinding.sampleDiffRecycler.apply {
            layoutManager = LinearLayoutManager(this@DiffUtilActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = articleAdapter
        }
    }

    private fun initActionButtons() {
        // 1. 点赞 +1（Payload 细粒度刷新：只更新点赞文本，不重绑标题）
        mBinding.sampleBtnPayloadLike.setOnClickListener {
            if (currentList.isNotEmpty()) {
                val targetId = currentList.first().id
                val newList = currentList.map {
                    if (it.id == targetId) it.copy(likes = it.likes + 1) else it.copy()
                }.toMutableList()
                applyDiffResult(newList)
            }
        }

        // 2. 修改标题（整项重新绑定）
        mBinding.sampleBtnUpdateTitle.setOnClickListener {
            if (currentList.isNotEmpty()) {
                val targetId = currentList.first().id
                val newList = currentList.map {
                    if (it.id == targetId) {
                        it.copy(title = "文章 [已更新标题 ${System.currentTimeMillis() % 1000}]")
                    } else it.copy()
                }.toMutableList()
                applyDiffResult(newList)
            }
        }

        // 3. 头部插入新数据（触发插入动画）
        mBinding.sampleBtnInsertItem.setOnClickListener {
            val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
            val newList = currentList.toMutableList().apply {
                add(0, ArticleItem(id = newId, title = "新增文章 #$newId (新鲜发布)", likes = 0))
            }
            applyDiffResult(newList)
        }

        // 4. 删除首条数据（触发移除动画）
        mBinding.sampleBtnDeleteItem.setOnClickListener {
            if (currentList.isNotEmpty()) {
                val newList = currentList.toMutableList().apply {
                    removeAt(0)
                }
                applyDiffResult(newList)
            }
        }

        // 5. 随机乱序（触发移动动画）
        mBinding.sampleBtnShuffleItems.setOnClickListener {
            if (currentList.size > 1) {
                val newList = currentList.toMutableList().apply {
                    shuffle()
                }
                applyDiffResult(newList)
            }
        }
    }

    /**
     * 核心流程：计算 Diff 并分发更新
     */
    private fun applyDiffResult(newList: MutableList<ArticleItem>) {
        val callback = ArticleAdapter.DiffCallback(currentList, newList)
        val diffResult = DiffUtil.calculateDiff(callback)

        currentList = newList
        articleAdapter.dataList = newList
        diffResult.dispatchUpdatesTo(articleAdapter)

        updateStatusInfo()
    }

    private fun updateStatusInfo() {
        mBinding.sampleDiffStatusText.text =
            "当前数据项: ${currentList.size} 篇 | 点击按钮或条目，观察局部刷新与 ItemAnimator 动画"
    }
}
