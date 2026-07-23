package com.example.william.my.core.javaws.server

import com.example.william.my.core.javaws.JavaWebSocketLogger
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

abstract class JavaWebSocketServerListener {

    open fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        JavaWebSocketLogger.debug("Server onOpen: ${conn.remoteSocketAddress}")
    }

    open fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        JavaWebSocketLogger.debug("Server onClose: code=$code reason=$reason remote=$remote")
    }

    open fun onMessage(conn: WebSocket, message: String) {
        JavaWebSocketLogger.debug("Server onMessage: $message")
    }

    open fun onError(conn: WebSocket?, ex: Exception) {
        JavaWebSocketLogger.error("Server onError: ${ex.message}", ex)
    }

    open fun onStart() {
        JavaWebSocketLogger.debug("Server onStart")
    }
}
