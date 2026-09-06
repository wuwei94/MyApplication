package com.example.william.my.core.javaws.server

import com.example.william.my.core.javaws.JavaWebSocketLogger
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * Java-WebSocket 服务端封装（基于 org.java_websocket.server.WebSocketServer）
 */
object JavaWebSocketServer {

    private var server: WebSocketServer? = null

    fun start(
        port: Int,
        listener: JavaWebSocketServerListener? = null,
    ): WebSocketServer {
        val address = InetSocketAddress(port)
        return start(address, listener)
    }

    fun start(
        address: InetSocketAddress,
        listener: JavaWebSocketServerListener? = null,
    ): WebSocketServer {
        server?.stop()
        server = object : WebSocketServer(address) {
            override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                listener?.onOpen(conn, handshake)
            }

            override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                listener?.onClose(conn, code, reason, remote)
            }

            override fun onMessage(conn: WebSocket, message: String) {
                listener?.onMessage(conn, message)
            }

            override fun onError(conn: WebSocket?, ex: Exception) {
                listener?.onError(conn, ex)
            }

            override fun onStart() {
                listener?.onStart()
            }
        }

        server?.start()
        return server!!
    }

    fun stop() {
        try {
            server?.stop()
        } catch (e: Exception) {
            JavaWebSocketLogger.error("stop failed", e)
        }
        server = null
    }

    fun broadcast(message: String) {
        server?.broadcast(message)
    }

    fun broadcast(bytes: ByteArray) {
        server?.broadcast(bytes)
    }

    fun getConnections(): Int = server?.connections?.size ?: 0

    fun isRunning(): Boolean = server != null
}
