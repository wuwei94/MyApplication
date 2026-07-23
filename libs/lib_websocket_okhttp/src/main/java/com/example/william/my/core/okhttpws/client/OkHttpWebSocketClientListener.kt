package com.example.william.my.core.okhttpws.client

import com.example.william.my.core.okhttpws.OkHttpWebSocketLogger
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

/**
 * OkHttp WebSocket 客户端监听器
 *
 * 提供 WebSocket 连接的回调接口，用于普通版本（非 RxJava）
 */
abstract class OkHttpWebSocketClientListener {

    /**
     * 连接成功时回调
     */
    open fun onOpen(webSocket: WebSocket, response: Response) {
        OkHttpWebSocketLogger.debug("Client onOpen: ${response.code}")
    }

    /**
     * 收到文本消息时回调
     */
    open fun onMessage(webSocket: WebSocket, text: String) {
        OkHttpWebSocketLogger.debug("Client onMessage: $text")
    }

    /**
     * 收到字节消息时回调
     */
    open fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        OkHttpWebSocketLogger.debug("Client onMessage: $bytes")
    }

    /**
     * 连接正在关闭时回调
     */
    open fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        OkHttpWebSocketLogger.debug("Client onClosing: code=$code reason=$reason")
    }

    /**
     * 连接已关闭时回调
     */
    open fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        OkHttpWebSocketLogger.debug("Client onClosed: code=$code reason=$reason")
    }

    /**
     * 连接失败时回调
     */
    open fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        OkHttpWebSocketLogger.error("Client onFailure: ${t.message}", t)
    }
}
