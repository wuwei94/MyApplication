package com.example.william.my.core.okhttpws.client

import com.example.william.my.core.okhttpws.OkHttpWebSocketInfo
import com.example.william.my.core.okhttpws.OkHttpWebSocketLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.ConcurrentHashMap

/**
 * OkHttp WebSocket 客户端（Kotlin Coroutines Flow 封装）
 *
 * 使用 callbackFlow 将 WebSocket 事件转为响应式 Flow 数据流。
 * 协程取消或生命周期结束时自动安全关闭连接。
 */
object OkHttpWebSocketClientFlow {

    private val defaultClient = OkHttpClient()
    private val webSocketMap = ConcurrentHashMap<String, WebSocket>()

    fun setWebSocket(url: String, webSocket: WebSocket) {
        webSocketMap[url] = webSocket
    }

    private fun getWebSocket(url: String): WebSocket? = webSocketMap[url]

    fun createWebSocket(url: String): Flow<OkHttpWebSocketInfo> = createWebSocket(url, Request.Builder().get().url(url).build(), defaultClient)

    fun createWebSocket(request: Request): Flow<OkHttpWebSocketInfo> = createWebSocket(request.url.toString(), request, defaultClient)

    fun createWebSocket(url: String, okHttpClient: OkHttpClient): Flow<OkHttpWebSocketInfo> = createWebSocket(url, Request.Builder().get().url(url).build(), okHttpClient)

    fun createWebSocket(
        request: Request,
        okHttpClient: OkHttpClient,
    ): Flow<OkHttpWebSocketInfo> = createWebSocket(request.url.toString(), request, okHttpClient)

    private fun createWebSocket(
        url: String,
        request: Request,
        okHttpClient: OkHttpClient,
    ): Flow<OkHttpWebSocketInfo> = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocketMap[url] = webSocket
                OkHttpWebSocketLogger.debug("Flow onOpen: $url")
                trySend(OkHttpWebSocketInfo.Open(webSocket))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                OkHttpWebSocketLogger.debug("Flow onMessage (Text): $text")
                trySend(OkHttpWebSocketInfo.TextMessage(webSocket, text))
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                OkHttpWebSocketLogger.debug("Flow onMessage (Bytes): $bytes")
                trySend(OkHttpWebSocketInfo.BytesMessage(webSocket, bytes))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                webSocketMap.remove(url)
                OkHttpWebSocketLogger.debug("Flow onClosed: code=$code reason=$reason")
                trySend(OkHttpWebSocketInfo.Closed(code, reason))
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                webSocketMap.remove(url)
                OkHttpWebSocketLogger.error("Flow onFailure: ${t.message}", t)
                trySend(OkHttpWebSocketInfo.Error(if (t is Exception) t else Exception(t)))
                close(t)
            }
        }

        val webSocket = okHttpClient.newWebSocket(request, listener)
        webSocketMap[url] = webSocket

        awaitClose {
            OkHttpWebSocketLogger.debug("Flow awaitClose: $url")
            try {
                webSocket.cancel()
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("cancel failed in awaitClose: $url", e)
            }
            webSocketMap.remove(url)
        }
    }.flowOn(Dispatchers.IO)

    fun send(url: String, message: String): Boolean {
        val webSocket = getWebSocket(url) ?: return false
        return try {
            webSocket.send(message)
        } catch (e: Exception) {
            OkHttpWebSocketLogger.error("send failed: $url", e)
            false
        }
    }

    fun send(url: String, bytes: ByteString): Boolean {
        val webSocket = getWebSocket(url) ?: return false
        return try {
            webSocket.send(bytes)
        } catch (e: Exception) {
            OkHttpWebSocketLogger.error("send bytes failed: $url", e)
            false
        }
    }

    fun send(request: Request, message: String): Boolean = send(request.url.toString(), message)

    fun close(url: String, code: Int = 1000, reason: String = "") {
        getWebSocket(url)?.let { webSocket ->
            try {
                webSocket.close(code, reason)
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("close failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    fun close(request: Request, code: Int = 1000, reason: String = "") {
        close(request.url.toString(), code, reason)
    }

    fun cancel(url: String) {
        getWebSocket(url)?.let { webSocket ->
            try {
                webSocket.cancel()
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("cancel failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    fun cancel(request: Request) {
        cancel(request.url.toString())
    }

    fun closeAll() {
        webSocketMap.values.forEach { webSocket ->
            try {
                webSocket.close(1000, "Client closing")
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("closeAll failed", e)
            }
        }
        webSocketMap.clear()
    }

    fun isConnected(url: String): Boolean = webSocketMap.containsKey(url)
}
