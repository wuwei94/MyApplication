package com.example.william.my.core.imageloader

/**
 * 图片加载配置项（磁盘缓存策略与内存缓存开关）
 */
data class ImageOptions(
    val skipMemoryCache: Boolean = false,
    val cacheStrategy: CacheStrategy = CacheStrategy.ALL,
) {
    /**
     * 磁盘缓存策略
     */
    enum class CacheStrategy {
        ALL,
        NONE,
        DATA,
        RESOURCE,
    }
}
