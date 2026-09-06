package com.example.william.my.core.okhttpws

import okhttp3.WebSocket
import okio.ByteString

/**
 * OkHttp WebSocket 客户端事件信息（密封类，表示连接、消息、关闭、错误等事件）
 */
sealed class OkHttpWebSocketInfo {
    data class Open(val webSocket: WebSocket) : OkHttpWebSocketInfo()
    data class TextMessage(val webSocket: WebSocket, val text: String) : OkHttpWebSocketInfo()
    data class BytesMessage(val webSocket: WebSocket, val bytes: ByteString) : OkHttpWebSocketInfo()
    data class Closed(val code: Int, val reason: String) : OkHttpWebSocketInfo()
    data class Error(val exception: Exception) : OkHttpWebSocketInfo()
}
