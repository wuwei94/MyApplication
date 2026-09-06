package com.example.william.my.core.javaws.client

import com.example.william.my.core.javaws.JavaWebSocketInfo
import com.example.william.my.core.javaws.JavaWebSocketLogger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.java_websocket.client.WebSocketClient
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Java-WebSocket 客户端 RxJava 封装
 */
object JavaWebSocketClientRx {

    private val webSocketMap = ConcurrentHashMap<String, WebSocketClient>()
    private val disposableMap = ConcurrentHashMap<String, Disposable>()

    fun setWebSocket(url: String, client: WebSocketClient) {
        webSocketMap[url] = client
    }

    private fun getWebSocket(url: String): WebSocketClient? = webSocketMap[url]

    fun createWebSocket(
        url: String,
        autoReconnect: Boolean = true,
        reconnectInterval: Long = 5000,
    ): Observable<JavaWebSocketInfo> = createWebSocket(URI(url), autoReconnect, reconnectInterval)

    fun createWebSocket(
        uri: URI,
        autoReconnect: Boolean = true,
        reconnectInterval: Long = 5000,
    ): Observable<JavaWebSocketInfo> {
        val url = uri.toString()

        // 如果已有缓存的订阅，直接返回当前连接状态
        disposableMap[url]?.let { existing ->
            if (!existing.isDisposed) {
                val client = webSocketMap[url]
                return if (client != null && client.isOpen) {
                    Observable.just(JavaWebSocketInfo.Open(client))
                } else {
                    Observable.empty()
                }
            }
        }

        return Observable.create(JavaWebSocketRxOnSubscribe(uri, autoReconnect, reconnectInterval))
            .retry { throwable ->
                throwable is java.net.SocketException || throwable is java.io.EOFException
            }
            .doOnDispose {
                webSocketMap.remove(url)
                disposableMap.remove(url)
            }
            .doOnNext { info ->
                when (info) {
                    is JavaWebSocketInfo.Open -> webSocketMap[url] = info.webSocket as WebSocketClient
                    is JavaWebSocketInfo.Closed -> {
                        webSocketMap.remove(url)
                        disposableMap.remove(url)
                    }
                    else -> { /* no-op */ }
                }
            }
            .share()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

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
                client.closeBlocking()
            } catch (e: Exception) {
                JavaWebSocketLogger.error("close failed: $url", e)
            }
        }
        webSocketMap.remove(url)
        disposableMap[url]?.dispose()
        disposableMap.remove(url)
    }

    fun cancel(url: String) {
        getWebSocket(url)?.let { client ->
            try {
                client.closeConnection(
                    org.java_websocket.framing.CloseFrame.ABNORMAL_CLOSE,
                    "Client cancel",
                )
            } catch (e: Exception) {
                JavaWebSocketLogger.error("cancel failed: $url", e)
            }
        }
        webSocketMap.remove(url)
        disposableMap[url]?.dispose()
        disposableMap.remove(url)
    }

    fun subscribe(url: String): Disposable {
        disposableMap[url]?.dispose()
        val disposable = createWebSocket(url).subscribe()
        disposableMap[url] = disposable
        return disposable
    }
}
