package com.example.william.my.core.javaws.client

import android.os.Handler
import android.os.Looper
import com.example.william.my.core.javaws.JavaWebSocketLogger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

object JavaWebSocketClient {

    private val clients = ConcurrentHashMap<String, WebSocketClient>()
    private val reconnectRunnables = ConcurrentHashMap<String, Runnable>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun connect(
        url: String,
        autoReconnect: Boolean = true,
        reconnectInterval: Long = 5000,
        listener: JavaWebSocketClientListener? = null
    ): WebSocketClient {
        return connect(URI(url), autoReconnect, reconnectInterval, listener)
    }

    fun connect(
        uri: URI,
        autoReconnect: Boolean = true,
        reconnectInterval: Long = 5000,
        listener: JavaWebSocketClientListener? = null
    ): WebSocketClient {
        val url = uri.toString()

        // 如果已有活跃连接，直接返回
        clients[url]?.let { existing ->
            if (existing.isOpen) return existing
        }

        // 取消之前的重连
        cancelReconnect(url)

        val client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                mainHandler.post { listener?.onOpen(this, handshakedata) }
            }

            override fun onMessage(message: String) {
                mainHandler.post { listener?.onMessage(this, message) }
            }

            override fun onMessage(message: ByteBuffer) {
                mainHandler.post { listener?.onMessage(this, message) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                clients.remove(url)
                mainHandler.post { listener?.onClose(this, code, reason, remote) }
                // 手动重连（Java-WebSocket 1.6.0 无 isAutoReconnect）
                if (autoReconnect) {
                    val runnable = Runnable {
                        reconnectRunnables.remove(url)
                        JavaWebSocketLogger.debug("Reconnecting: $uri")
                        try {
                            connect(uri, autoReconnect, reconnectInterval, listener)
                        } catch (e: Exception) {
                            JavaWebSocketLogger.error("Reconnect failed: $uri", e)
                        }
                    }
                    reconnectRunnables[url] = runnable
                    mainHandler.postDelayed(runnable, reconnectInterval)
                }
            }

            override fun onError(ex: Exception) {
                mainHandler.post { listener?.onError(this, ex) }
            }
        }

        clients[url] = client
        client.connect()
        return client
    }

    fun send(url: String, message: String): Boolean {
        val client = clients[url] ?: return false
        return try {
            client.send(message)
            true
        } catch (e: Exception) {
            JavaWebSocketLogger.error("send failed: $url", e)
            false
        }
    }

    fun send(url: String, bytes: ByteArray): Boolean {
        val client = clients[url] ?: return false
        return try {
            client.send(bytes)
            true
        } catch (e: Exception) {
            JavaWebSocketLogger.error("send bytes failed: $url", e)
            false
        }
    }

    fun close(url: String) {
        cancelReconnect(url)
        clients[url]?.let { client ->
            try {
                client.closeBlocking()
            } catch (e: Exception) {
                JavaWebSocketLogger.error("close failed: $url", e)
            }
        }
        clients.remove(url)
    }

    fun cancel(url: String) {
        cancelReconnect(url)
        clients[url]?.let { client ->
            try {
                client.closeConnection(
                    org.java_websocket.framing.CloseFrame.ABNORMAL_CLOSE,
                    "Client cancel"
                )
            } catch (e: Exception) {
                JavaWebSocketLogger.error("cancel failed: $url", e)
            }
        }
        clients.remove(url)
    }

    fun closeAll() {
        reconnectRunnables.keys.forEach { cancelReconnect(it) }
        reconnectRunnables.clear()
        clients.values.forEach { client ->
            try {
                client.closeBlocking()
            } catch (e: Exception) {
                JavaWebSocketLogger.error("closeAll failed", e)
            }
        }
        clients.clear()
    }

    fun isConnected(url: String): Boolean {
        return clients[url]?.isOpen == true
    }

    private fun cancelReconnect(url: String) {
        reconnectRunnables.remove(url)?.let { runnable ->
            mainHandler.removeCallbacks(runnable)
        }
    }
}
