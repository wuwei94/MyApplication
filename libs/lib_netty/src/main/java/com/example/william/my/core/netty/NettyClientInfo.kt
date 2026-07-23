package com.example.william.my.core.netty

/**
 * Netty 客户端消息信息
 *
 * 用于 RxJava 版本的 WebSocket 事件封装
 */
sealed class NettyClientInfo {
    /**
     * 连接成功
     */
    data class Open(val host: String, val port: Int) : NettyClientInfo()

    /**
     * 收到文本消息
     */
    data class TextMessage(val message: String) : NettyClientInfo()

    /**
     * 连接关闭
     */
    data class Closed(val reason: String = "") : NettyClientInfo()

    /**
     * 连接错误
     */
    data class Error(val exception: Throwable) : NettyClientInfo()
}
