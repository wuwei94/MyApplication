package com.example.william.my.core.imageloader

data class ImageOptions(
    val skipMemoryCache: Boolean = false,
    val cacheStrategy: CacheStrategy = CacheStrategy.ALL,
) {
    enum class CacheStrategy {
        ALL,
        NONE,
        DATA,
        RESOURCE,
    }
}
