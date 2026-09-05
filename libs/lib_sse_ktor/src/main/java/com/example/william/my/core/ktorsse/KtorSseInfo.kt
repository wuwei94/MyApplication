package com.example.william.my.core.ktorsse

/**
 * Ktor SSE 事件密封类
 */
sealed class KtorSseInfo {
    /** 连接已建立 */
    data object Open : KtorSseInfo()

    /** 收到 SSE 事件数据 */
    data class Event(
        val id: String?,
        val event: String?,
        val data: String?,
    ) : KtorSseInfo()

    /** 连接已关闭 */
    data class Closed(val reason: String = "") : KtorSseInfo()

    /** 连接发生异常 */
    data class Error(val throwable: Throwable) : KtorSseInfo()
}
