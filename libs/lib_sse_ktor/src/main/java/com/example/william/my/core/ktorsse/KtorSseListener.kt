package com.example.william.my.core.ktorsse

/**
 * Ktor SSE 回调监听器
 */
interface KtorSseListener {

    /**
     * 连接已建立
     */
    fun onOpen() {}

    /**
     * 收到 SSE 事件数据
     */
    fun onEvent(id: String?, event: String?, data: String?) {}

    /**
     * 连接已关闭
     */
    fun onClosed(reason: String = "") {}

    /**
     * 连接发生异常或失败
     */
    fun onFailure(t: Throwable) {}
}
