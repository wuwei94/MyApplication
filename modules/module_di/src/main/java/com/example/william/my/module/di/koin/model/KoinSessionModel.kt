package com.example.william.my.module.di.koin.model

/**
 * 演示动态运行时传参构建的会话对象
 */
class KoinUserProfileSession(
    private val userId: String,
    private val tracker: KoinAnalyticsTracker,
) {
    fun getSessionDetails(): String = "UserSession [用户ID: $userId] - ${tracker.logEvent("session_access")}"
}

/**
 * 局部 Scope 作用域会话对象
 */
class KoinScopedSession {
    val info: String = "KoinScopedSession 处于独立作用域生命周期内"
}
