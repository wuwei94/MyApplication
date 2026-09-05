package com.example.william.my.module.performance.manager

import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.performance.manager.RecycledViewPoolManager.clear

/**
 * 全局 RecyclerView 视图复用池管理器（RecycledViewPool Manager）
 *
 * 针对 App 中高频同质化卡片（如首页推荐流、搜索流、分类专区），提供跨页面/跨列表的全局 ViewHolder 复用能力：
 * 1. 核心机制：维护全局单例的 [RecyclerView.RecycledViewPool]，多个 Activity/Fragment 可共享同一个池；
 * 2. 性能收益：跨页面跳转时 0 成本复用已存在的 ViewHolder，大幅减少 onCreateViewHolder 次数与渲染耗时；
 * 3. 容量控制：支持按 viewType 定制最大缓存容量（默认 5）；
 * 4. 安全规范：
 *    - 推荐使用全局唯一的 XML LayoutId 或枚举常量作为 viewType，防止多业务类型冲突；
 *    - 在适当的业务退出或内存紧张时调用 [clear] 释放缓存引用。
 */
object RecycledViewPoolManager {

    private val pool = RecyclerView.RecycledViewPool()

    /**
     * 获取全局底层的 RecycledViewPool 实例
     */
    fun getPool(): RecyclerView.RecycledViewPool = pool

    /**
     * 设置指定 viewType 的最大缓存容量
     */
    fun setMaxRecycledViews(viewType: Int, maxCount: Int) {
        pool.setMaxRecycledViews(viewType, maxCount)
    }

    /**
     * 获取指定 viewType 当前在池中的缓存数量
     */
    fun getRecycledViewCount(viewType: Int): Int = pool.getRecycledViewCount(viewType)

    /**
     * 清空全局复用池
     */
    fun clear() {
        pool.clear()
    }
}
