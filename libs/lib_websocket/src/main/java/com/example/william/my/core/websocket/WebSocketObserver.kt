package com.example.william.my.core.websocket

import io.reactivex.rxjava3.observers.DisposableObserver
import okhttp3.WebSocket
import okio.ByteString

abstract class WebSocketObserver : DisposableObserver<WebSocketInfo>() {

    override fun onNext(info: WebSocketInfo) {
        when (info) {
            is WebSocketInfo.Open -> onOpen(info.webSocket)
            is WebSocketInfo.TextMessage -> onMessage(info.webSocket, info.text)
            is WebSocketInfo.BytesMessage -> onMessage(info.webSocket, info.bytes)
            is WebSocketInfo.Reconnect -> onReconnect()
            is WebSocketInfo.Closed -> onClosed(info.code, info.reason)
        }
    }

    override fun onError(e: Throwable) {
        WebSocketLogger.error("WebSocket error", e)
    }

    override fun onComplete() {
        // 空实现 — 不在此处自动 dispose。
        // share() 上游在所有 subscriber dispose 后会触发 onComplete，
        // 如果此处也 dispose 会打断重连逻辑。
    }

    protected open fun onOpen(webSocket: WebSocket) {}
    protected open fun onMessage(webSocket: WebSocket, text: String) {}
    protected open fun onMessage(webSocket: WebSocket, bytes: ByteString) {}
    protected open fun onReconnect() {}
    protected open fun onClosed(code: Int, reason: String) {}
}
