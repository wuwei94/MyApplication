package com.example.william.my.core.server.javaws

import com.example.william.my.core.javaws.server.JavaWebSocketServer as CoreJavaWebSocketServer
import com.example.william.my.core.javaws.server.JavaWebSocketServerListener
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

class JavaWebSocketServer {

    private var listener: OnMessageListener? = null

    fun setOnMessageListener(listener: OnMessageListener) {
        this.listener = listener
    }

    fun start(port: Int = DEFAULT_PORT) {
        CoreJavaWebSocketServer.start(port, object : JavaWebSocketServerListener() {
            override fun onStart() {
                listener?.onStart()
            }

            override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                listener?.onClientConnected(conn.remoteSocketAddress.toString())
            }

            override fun onMessage(conn: WebSocket, message: String) {
                listener?.onMessage(conn.remoteSocketAddress.toString(), message)
            }

            override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                listener?.onClientDisconnected(conn.remoteSocketAddress.toString())
            }

            override fun onError(conn: WebSocket?, ex: Exception) {
                listener?.onError(ex)
            }
        })
    }

    fun stop() {
        CoreJavaWebSocketServer.stop()
    }

    fun isRunning(): Boolean {
        return CoreJavaWebSocketServer.isRunning()
    }

    fun broadcast(message: String) {
        CoreJavaWebSocketServer.broadcast(message)
    }

    interface OnMessageListener {
        fun onStart()
        fun onClientConnected(remoteAddress: String)
        fun onMessage(remoteAddress: String, message: String)
        fun onClientDisconnected(remoteAddress: String)
        fun onError(throwable: Exception)
    }

    companion object {
        const val DEFAULT_PORT = 5566
    }
}
