package com.example.william.my.module.performance.activity

import android.os.Bundle
import android.util.LruCache
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.bean.UserProfile

/**
 * LruCache 内存缓存设计与标准写法
 *
 * 本示例演示 LruCache 在 Repository 数据层中的标准设计与实践：
 * 1. 缓存读取策略（Cache-Aside Pattern）：先读内存缓存 -> 命中则立即返回 -> 未命中回源拉取并写入缓存。
 * 2. sizeOf(key, value)：自定义缓存条目的权重计算（如按对象数或按字节大小）。
 * 3. entryRemoved(evicted, key, oldValue, newValue)：监听条目被淘汰或移除事件，用于释放下层关联资源。
 * 4. evictAll()：清空内存缓存。
 */
@Route(path = RouterPath.Performance.LruCache)
class LruCacheActivity : BasicResponseActivity() {

    // 容量为 3 个 Key 的演示缓存
    private lateinit var userProfileCache: LruCache<String, UserProfile>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("LruCache 内存缓存设计模式示例\n演示 Cache-Aside 回源读取、容量自动淘汰与 entryRemoved 监听")

        // 初始化 LruCache，设置最大容量为 3 个条目
        userProfileCache = object : LruCache<String, UserProfile>(3) {
            override fun sizeOf(key: String, value: UserProfile): Int {
                return 1 // 每个对象计 1 个单位
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: UserProfile,
                newValue: UserProfile?,
            ) {
                if (evicted) {
                    appendLog("  [LRU 淘汰事件] Key: $key（用户：${oldValue.name}）因容量超出被自动淘汰！")
                }
            }
        }
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 写入并读取用户缓存（首次未命中 -> 回源拉取并存入）",
        "2. 再次读取相同用户（直接命中 LruCache 内存缓存）",
        "3. 连续写入多个用户（触发 LRU 容量淘汰机制）",
        "4. 查看当前缓存池内容与状态",
        "5. 清空缓存（evictAll）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> fetchUserWithCache("user_101")
            1 -> fetchUserWithCache("user_101")
            2 -> testCapacityEviction()
            3 -> showCacheStatus()
            4 -> clearCache()
        }
    }

    /**
     * 示例 1 & 2：标准的 Cache-Aside 回源加载模式
     */
    private fun fetchUserWithCache(userId: String) {
        appendLog("【请求用户 $userId】")
        // 1. 尝试从 LruCache 中读取
        val cached = userProfileCache.get(userId)
        if (cached != null) {
            appendLogAccent("  [命中内存缓存] 用户名：${cached.name}，等级：${cached.level}（0 网络耗时）")
            return
        }

        // 2. 缓存未命中，模拟回源请求并写入缓存
        appendLog("  [未命中缓存] 正在回源拉取最新数据...")
        val freshUser = mockFetchFromNetworkOrDb(userId)
        userProfileCache.put(userId, freshUser)
        appendLogAccent("  [回源成功并写入缓存] 用户名：${freshUser.name}")
    }

    /**
     * 示例 3：演示容量超出时的 LRU 淘汰
     */
    private fun testCapacityEviction() {
        appendLog("【示例 3】连续写入 user_201, user_202, user_203, user_204（最大容量为 3）...")
        for (i in 201..204) {
            val key = "user_$i"
            val user = UserProfile(id = key, name = "User $i", level = i % 10)
            userProfileCache.put(key, user)
            appendLog("  写入 $key，当前缓存数：${userProfileCache.size()} / 最大上限：${userProfileCache.maxSize()}")
        }
    }

    /**
     * 示例 4：查看当前缓存内容
     */
    private fun showCacheStatus() {
        appendLogAccent("【当前缓存池状态】")
        appendLog("  - 当前大小：${userProfileCache.size()} / 容量上限：${userProfileCache.maxSize()}")
        appendLog("  - 命中次数：${userProfileCache.hitCount()} | 未命中次数：${userProfileCache.missCount()}")
        appendLog("  - 当前包含的 Key：${userProfileCache.snapshot().keys}")
    }

    /**
     * 示例 5：清空缓存
     */
    private fun clearCache() {
        userProfileCache.evictAll()
        appendLogAccent("已调用 userProfileCache.evictAll() 清空全部内存缓存！")
    }

    private fun mockFetchFromNetworkOrDb(userId: String): UserProfile = UserProfile(id = userId, name = "Name of $userId", level = 99)
}
