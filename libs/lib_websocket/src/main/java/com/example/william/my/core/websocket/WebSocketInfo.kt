package com.example.william.my.core.websocket

import okhttp3.WebSocket
import okio.ByteString

sealed class WebSocketInfo {
    data class Open(val webSocket: WebSocket) : WebSocketInfo()
    data class TextMessage(val webSocket: WebSocket, val text: String) : WebSocketInfo()
    data class BytesMessage(val webSocket: WebSocket, val bytes: ByteString) : WebSocketInfo()
    data class Closed(val code: Int, val reason: String) : WebSocketInfo()
    data object Reconnect : WebSocketInfo()
}
