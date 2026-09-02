package com.example.william.my.core.javaws.client

import com.example.william.my.core.javaws.JavaWebSocketInfo
import com.example.william.my.core.javaws.JavaWebSocketLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Java-WebSocket 客户端（Kotlin Coroutines Flow 封装）
 *
 * 使用 callbackFlow 将 Java-WebSocket 事件转为响应式 Flow 数据流。
 * 协程取消或生命周期结束时自动安全关闭连接。
 */
object JavaWebSocketClientFlow {

    private val webSocketMap = ConcurrentHashMap<String, WebSocketClient>()

    fun setWebSocket(url: String, client: WebSocketClient) {
        webSocketMap[url] = client
    }

    private fun getWebSocket(url: String): WebSocketClient? {
        return webSocketMap[url]
    }

    fun createWebSocket(url: String): Flow<JavaWebSocketInfo> {
        return createWebSocket(URI(url))
    }

    fun createWebSocket(uri: URI): Flow<JavaWebSocketInfo> = callbackFlow {
        val url = uri.toString()

        val client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                webSocketMap[url] = this
                JavaWebSocketLogger.debug("Flow onOpen: $url, status=${handshakedata.httpStatus}")
                trySend(JavaWebSocketInfo.Open(this))
            }

            override fun onMessage(message: String) {
                JavaWebSocketLogger.debug("Flow onMessage (Text): $message")
                trySend(JavaWebSocketInfo.TextMessage(this, message))
            }

            override fun onMessage(bytes: ByteBuffer) {
                val array = ByteArray(bytes.remaining())
                bytes.get(array)
                JavaWebSocketLogger.debug("Flow onMessage (Bytes): ${array.size} bytes")
                trySend(JavaWebSocketInfo.BytesMessage(this, array))
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                webSocketMap.remove(url)
                JavaWebSocketLogger.debug("Flow onClose: code=$code, reason=$reason, remote=$remote")
                trySend(JavaWebSocketInfo.Closed(code, reason, remote))
                close()
            }

            override fun onError(ex: Exception) {
                webSocketMap.remove(url)
                JavaWebSocketLogger.error("Flow onError: ${ex.message}", ex)
                trySend(JavaWebSocketInfo.Error(ex))
                close(ex)
            }
        }

        webSocketMap[url] = client
        client.connect()

        awaitClose {
            JavaWebSocketLogger.debug("Flow awaitClose: $url")
            try {
                if (!client.isClosed) {
                    client.close()
                }
            } catch (e: Exception) {
                JavaWebSocketLogger.error("close failed in awaitClose: $url", e)
            }
            webSocketMap.remove(url)
        }
    }.flowOn(Dispatchers.IO)

    fun send(url: String, message: String): Boolean {
        val client = getWebSocket(url) ?: return false
        return try {
            client.send(message)
            true
        } catch (e: Exception) {
            JavaWebSocketLogger.error("send failed: $url", e)
            false
        }
    }

    fun send(url: String, bytes: ByteArray): Boolean {
        val client = getWebSocket(url) ?: return false
        return try {
            client.send(bytes)
            true
        } catch (e: Exception) {
            JavaWebSocketLogger.error("send bytes failed: $url", e)
            false
        }
    }

    fun close(url: String) {
        getWebSocket(url)?.let { client ->
            try {
                client.close()
            } catch (e: Exception) {
                JavaWebSocketLogger.error("close failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    fun cancel(url: String) {
        getWebSocket(url)?.let { client ->
            try {
                client.closeConnection(
                    org.java_websocket.framing.CloseFrame.ABNORMAL_CLOSE,
                    "Client cancel"
                )
            } catch (e: Exception) {
                JavaWebSocketLogger.error("cancel failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    fun isConnected(url: String): Boolean {
        val client = webSocketMap[url]
        return client != null && client.isOpen
    }
}
