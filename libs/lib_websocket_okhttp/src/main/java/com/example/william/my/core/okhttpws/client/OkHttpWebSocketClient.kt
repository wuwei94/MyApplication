package com.example.william.my.core.okhttpws.client

import android.os.Handler
import android.os.Looper
import com.example.william.my.core.okhttpws.OkHttpWebSocketLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.ConcurrentHashMap

/**
 * OkHttp WebSocket 客户端（普通版本）
 *
 * 使用回调方式处理 WebSocket 事件，不依赖 RxJava
 * 如果需要 RxJava 封装，请使用 OkHttpWebSocketClientRx
 */
object OkHttpWebSocketClient {

    private val defaultClient = OkHttpClient()
    private val webSocketMap = ConcurrentHashMap<String, WebSocket>()
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 连接到 WebSocket 服务器
     *
     * @param url WebSocket 服务器地址
     * @param listener 事件监听器
     * @return WebSocket 实例
     */
    fun connect(
        url: String,
        listener: OkHttpWebSocketClientListener? = null,
    ): WebSocket = connect(url, Request.Builder().get().url(url).build(), defaultClient, listener)

    /**
     * 连接到 WebSocket 服务器（自定义 Request）
     *
     * @param request HTTP 请求
     * @param listener 事件监听器
     * @return WebSocket 实例
     */
    fun connect(
        request: Request,
        listener: OkHttpWebSocketClientListener? = null,
    ): WebSocket = connect(request.url.toString(), request, defaultClient, listener)

    /**
     * 连接到 WebSocket 服务器（自定义 OkHttpClient）
     *
     * @param url WebSocket 服务器地址
     * @param okHttpClient OkHttp 客户端
     * @param listener 事件监听器
     * @return WebSocket 实例
     */
    fun connect(
        url: String,
        okHttpClient: OkHttpClient,
        listener: OkHttpWebSocketClientListener? = null,
    ): WebSocket = connect(url, Request.Builder().get().url(url).build(), okHttpClient, listener)

    /**
     * 连接到 WebSocket 服务器（完整参数）
     *
     * @param url WebSocket 服务器地址
     * @param request HTTP 请求
     * @param okHttpClient OkHttp 客户端
     * @param listener 事件监听器
     * @return WebSocket 实例
     */
    fun connect(
        url: String,
        request: Request,
        okHttpClient: OkHttpClient,
        listener: OkHttpWebSocketClientListener? = null,
    ): WebSocket {
        // 如果已有活跃连接，直接返回
        webSocketMap[url]?.let { existing ->
            return existing
        }

        val webSocket = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocketMap[url] = webSocket
                    mainHandler.post { listener?.onOpen(webSocket, response) }
                    OkHttpWebSocketLogger.debug("Client onOpen: $url")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    mainHandler.post { listener?.onMessage(webSocket, text) }
                    OkHttpWebSocketLogger.debug("Client onMessage: $text")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    mainHandler.post { listener?.onMessage(webSocket, bytes) }
                    OkHttpWebSocketLogger.debug("Client onMessage: $bytes")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    webSocketMap.remove(url)
                    mainHandler.post { listener?.onClosed(webSocket, code, reason) }
                    OkHttpWebSocketLogger.debug("Client onClosed: code=$code reason=$reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    webSocketMap.remove(url)
                    mainHandler.post { listener?.onFailure(webSocket, t, response) }
                    OkHttpWebSocketLogger.error("Client onFailure: ${t.message}", t)
                }
            },
        )

        webSocketMap[url] = webSocket
        return webSocket
    }

    /**
     * 发送文本消息
     *
     * @param url WebSocket 地址
     * @param message 文本消息
     * @return 是否发送成功
     */
    fun send(url: String, message: String): Boolean {
        val webSocket = webSocketMap[url] ?: return false
        return try {
            webSocket.send(message)
        } catch (e: Exception) {
            OkHttpWebSocketLogger.error("send failed: $url", e)
            false
        }
    }

    /**
     * 发送字节消息
     *
     * @param url WebSocket 地址
     * @param bytes 字节数据
     * @return 是否发送成功
     */
    fun send(url: String, bytes: ByteString): Boolean {
        val webSocket = webSocketMap[url] ?: return false
        return try {
            webSocket.send(bytes)
        } catch (e: Exception) {
            OkHttpWebSocketLogger.error("send bytes failed: $url", e)
            false
        }
    }

    /**
     * 关闭指定连接
     *
     * @param url WebSocket 地址
     * @param code 关闭码
     * @param reason 关闭原因
     */
    fun close(url: String, code: Int = 1000, reason: String = "") {
        webSocketMap[url]?.let { webSocket ->
            try {
                webSocket.close(code, reason)
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("close failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    /**
     * 取消指定连接
     *
     * @param url WebSocket 地址
     */
    fun cancel(url: String) {
        webSocketMap[url]?.let { webSocket ->
            try {
                webSocket.cancel()
            } catch (e: Exception) {
                OkHttpWebSocketLogger.error("cancel failed: $url", e)
            }
        }
        webSocketMap.remove(url)
    }

    /**
     * 关闭所有连接
     */
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

    /**
     * 检查连接状态
     *
     * @param url WebSocket 地址
     * @return 是否已连接
     */
    fun isConnected(url: String): Boolean = webSocketMap.containsKey(url)
}
