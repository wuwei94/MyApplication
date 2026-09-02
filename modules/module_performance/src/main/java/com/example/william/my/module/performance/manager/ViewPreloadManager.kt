package com.example.william.my.module.performance.manager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.example.william.my.module.performance.manager.ViewPreloadManager.getView
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap

/**
 * 视图预加载管理器（Pre-Inflation 模式）
 *
 * 针对复杂页面（如商品详情页、复杂列表卡片、大弹窗）提供后台异步提前解析与缓存管理：
 * 1. 核心机制：在前置页面（如列表页）或系统空闲期，在后台工作线程异步解析 XML 实例化 View，并缓存入池；
 * 2. 0ms 瞬间获取：目标页面（如详情页）直接调用 [getView] 从池中取用已就绪的 View，彻底消除主线程 inflate 卡顿；
 * 3. 自动降级（Fail-Safe）：若缓存池未命中或为空，自动降级为同步 [LayoutInflater.inflate] 解析，保证业务高可用；
 * 4. 多 Layout 分桶与容量上限：按 layoutResId 独立隔离队列，支持配置最大缓存容量防止内存无界增长。
 */
object ViewPreloadManager {

    private const val DEFAULT_MAX_POOL_SIZE = 5

    private val viewPools = ConcurrentHashMap<Int, Queue<View>>()
    private val maxPoolSizeMap = ConcurrentHashMap<Int, Int>()

    /**
     * 为指定布局配置缓存池最大容量上限（默认 5）
     */
    fun setMaxCount(@LayoutRes layoutResId: Int, maxCount: Int) {
        maxPoolSizeMap[layoutResId] = maxCount
    }

    /**
     * 异步预加载指定数量的 View 入池
     *
     * @param context 建议传入当前 Activity 上下文或带有目标 Theme 的 ContextThemeWrapper
     * @param layoutResId 待预加载的 XML 布局资源 ID
     * @param count 预加载数量（默认 1）
     * @param onComplete 全部预加载完成的主线程回调（回调参数为本次成功入池的数量）
     */
    fun preload(
        context: Context,
        @LayoutRes layoutResId: Int,
        count: Int = 1,
        onComplete: ((loadedCount: Int) -> Unit)? = null
    ) {
        if (count <= 0) {
            onComplete?.invoke(0)
            return
        }

        val pool = viewPools.getOrPut(layoutResId) { LinkedList() }
        val maxCapacity = maxPoolSizeMap[layoutResId] ?: DEFAULT_MAX_POOL_SIZE
        val asyncInflater = AsyncLayoutInflater(context)

        var finishedCount = 0
        var addedCount = 0

        for (i in 0 until count) {
            asyncInflater.inflate(layoutResId, null) { view, _, _ ->
                synchronized(pool) {
                    if (pool.size < maxCapacity) {
                        pool.offer(view)
                        addedCount++
                    }
                }
                finishedCount++
                if (finishedCount == count) {
                    onComplete?.invoke(addedCount)
                }
            }
        }
    }

    /**
     * 获取指定布局的 View 实例
     *
     * - **命中缓存**：若池中有预加载好的 View，立即出池并 0ms 返回；
     * - **自动降级**：若池为空，自动走主线程同步 [LayoutInflater.inflate] 安全加载。
     */
    fun getView(
        context: Context,
        @LayoutRes layoutResId: Int,
        parent: ViewGroup? = null,
        attachToRoot: Boolean = false
    ): View {
        val pool = viewPools[layoutResId]
        if (pool != null) {
            synchronized(pool) {
                val cachedView = pool.poll()
                if (cachedView != null) {
                    return cachedView
                }
            }
        }
        // 缓存未命中时，安全降级为同步 inflate 加载
        return LayoutInflater.from(context).inflate(layoutResId, parent, attachToRoot)
    }

    /**
     * 判断指定布局在池中是否有就绪的缓存 View
     */
    fun hasCache(@LayoutRes layoutResId: Int): Boolean {
        val pool = viewPools[layoutResId] ?: return false
        synchronized(pool) {
            return pool.isNotEmpty()
        }
    }

    /**
     * 获取指定布局当前在池中的缓存数量
     */
    fun getPoolSize(@LayoutRes layoutResId: Int): Int {
        val pool = viewPools[layoutResId] ?: return 0
        synchronized(pool) {
            return pool.size
        }
    }

    /**
     * 清空指定布局的缓存池
     */
    fun clear(@LayoutRes layoutResId: Int) {
        val pool = viewPools.remove(layoutResId)
        if (pool != null) {
            synchronized(pool) {
                pool.clear()
            }
        }
    }

    /**
     * 清空全部缓存池（建议在内存紧张或退出多模块主流程时调用）
     */
    fun clearAll() {
        viewPools.values.forEach { pool ->
            synchronized(pool) {
                pool.clear()
            }
        }
        viewPools.clear()
    }
}
