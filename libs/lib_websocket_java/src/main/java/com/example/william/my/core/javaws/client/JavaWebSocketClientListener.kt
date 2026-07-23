package com.example.william.my.core.javaws.client

import com.example.william.my.core.javaws.JavaWebSocketLogger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

abstract class JavaWebSocketClientListener {

    open fun onOpen(webSocket: WebSocketClient, handshakedata: ServerHandshake) {
        JavaWebSocketLogger.debug("Client onOpen: ${webSocket.uri}")
    }

    open fun onMessage(webSocket: WebSocketClient, message: String) {
        JavaWebSocketLogger.debug("Client onMessage: $message")
    }

    open fun onMessage(webSocket: WebSocketClient, message: java.nio.ByteBuffer) {
        JavaWebSocketLogger.debug("Client onMessage ByteBuffer")
    }

    open fun onClose(webSocket: WebSocketClient, code: Int, reason: String?, remote: Boolean) {
        JavaWebSocketLogger.debug("Client onClose: code=$code reason=$reason remote=$remote")
    }

    open fun onError(webSocket: WebSocketClient, ex: Exception) {
        JavaWebSocketLogger.error("Client onError: ${ex.message}", ex)
    }
}
