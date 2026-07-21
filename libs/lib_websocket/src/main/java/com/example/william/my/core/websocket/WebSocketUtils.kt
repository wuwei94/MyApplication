package com.example.william.my.core.websocket

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.EOFException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException

object WebSocketUtils {

    private val defaultClient = OkHttpClient()
    private val webSocketMap = ConcurrentHashMap<String, WebSocket>()
    private val disposableMap = ConcurrentHashMap<String, Disposable>()

    fun setWebSocket(url: String, webSocket: WebSocket) {
        webSocketMap[url] = webSocket
    }

    private fun getWebSocket(url: String): WebSocket? {
        return webSocketMap[url]
    }

    fun createWebSocket(url: String): Observable<WebSocketInfo> {
        return createWebSocket(url, Request.Builder().get().url(url).build(), defaultClient)
    }

    fun createWebSocket(request: Request): Observable<WebSocketInfo> {
        return createWebSocket(request.url.toString(), request, defaultClient)
    }

    fun createWebSocket(url: String, okHttpClient: OkHttpClient): Observable<WebSocketInfo> {
        return createWebSocket(url, Request.Builder().get().url(url).build(), okHttpClient)
    }

    fun createWebSocket(request: Request, okHttpClient: OkHttpClient): Observable<WebSocketInfo> {
        return createWebSocket(request.url.toString(), request, okHttpClient)
    }

    private fun createWebSocket(
        url: String,
        request: Request,
        okHttpClient: OkHttpClient
    ): Observable<WebSocketInfo> {
        // 如果已有缓存的订阅，直接返回当前连接状态
        disposableMap[url]?.let { existing ->
            if (!existing.isDisposed) {
                val webSocket = webSocketMap[url]
                return if (webSocket != null) {
                    Observable.just(WebSocketInfo.Open(webSocket))
                } else {
                    Observable.empty()
                }
            }
        }

        return Observable.create(WebSocketOnSubscribe(url, request, okHttpClient))
            .retry { throwable ->
                throwable is EOFException || throwable is TimeoutException
            }
            .doOnDispose {
                webSocketMap.remove(url)
                disposableMap.remove(url)
            }
            .doOnNext { info ->
                when (info) {
                    is WebSocketInfo.Open -> webSocketMap[url] = info.webSocket
                    is WebSocketInfo.Closed -> {
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
        val webSocket = getWebSocket(url) ?: return false
        return try {
            webSocket.send(message)
        } catch (e: Exception) {
            WebSocketLogger.error("send failed: $url", e)
            false
        }
    }

    fun send(request: Request, message: String): Boolean {
        return send(request.url.toString(), message)
    }

    fun cancel(url: String) {
        getWebSocket(url)?.let { webSocket ->
            try {
                webSocket.cancel()
            } catch (e: Exception) {
                WebSocketLogger.error("cancel failed: $url", e)
            }
        }
        webSocketMap.remove(url)
        disposableMap[url]?.dispose()
        disposableMap.remove(url)
    }

    fun cancel(request: Request) {
        cancel(request.url.toString())
    }

    fun subscribe(url: String): Disposable {
        disposableMap[url]?.dispose()
        val disposable = createWebSocket(url).subscribe()
        disposableMap[url] = disposable
        return disposable
    }

    fun subscribe(request: Request): Disposable {
        val url = request.url.toString()
        disposableMap[url]?.dispose()
        val disposable = createWebSocket(request).subscribe()
        disposableMap[url] = disposable
        return disposable
    }
}
