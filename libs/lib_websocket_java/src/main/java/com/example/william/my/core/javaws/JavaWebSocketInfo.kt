package com.example.william.my.core.javaws

import org.java_websocket.WebSocket

sealed class JavaWebSocketInfo {
    data class Open(val webSocket: WebSocket) : JavaWebSocketInfo()
    data class TextMessage(val webSocket: WebSocket, val message: String) : JavaWebSocketInfo()
    data class BytesMessage(val webSocket: WebSocket, val bytes: ByteArray) : JavaWebSocketInfo() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BytesMessage) return false
            return webSocket == other.webSocket && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * webSocket.hashCode() + bytes.contentHashCode()
    }
    data class Closed(val code: Int, val reason: String, val remote: Boolean) : JavaWebSocketInfo()
    data class Error(val exception: Exception) : JavaWebSocketInfo()
}
