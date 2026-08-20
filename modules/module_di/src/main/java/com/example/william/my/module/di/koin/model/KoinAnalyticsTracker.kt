package com.example.william.my.module.di.koin.model

/**
 * 模拟打点分析服务（单例）
 */
class KoinAnalyticsTracker {
    fun logEvent(event: String): String = "AnalyticsTracker 记录事件 [$event]"
}
