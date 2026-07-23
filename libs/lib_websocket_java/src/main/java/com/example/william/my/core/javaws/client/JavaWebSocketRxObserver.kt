package com.example.william.my.core.javaws.client

import com.example.william.my.core.javaws.JavaWebSocketInfo
import com.example.william.my.core.javaws.JavaWebSocketLogger
import io.reactivex.rxjava3.observers.DisposableObserver
import org.java_websocket.client.WebSocketClient

abstract class JavaWebSocketRxObserver : DisposableObserver<JavaWebSocketInfo>() {

    override fun onNext(info: JavaWebSocketInfo) {
        when (info) {
            is JavaWebSocketInfo.Open -> onOpen(info.webSocket as WebSocketClient)
            is JavaWebSocketInfo.TextMessage -> onMessage(info.webSocket as WebSocketClient, info.message)
            is JavaWebSocketInfo.BytesMessage -> onMessage(info.webSocket as WebSocketClient, info.bytes)
            is JavaWebSocketInfo.Closed -> onClosed(info.code, info.reason, info.remote)
            is JavaWebSocketInfo.Error -> onError(info.exception)
        }
    }

    override fun onError(e: Throwable) {
        JavaWebSocketLogger.error("WebSocket error", e)
    }

    override fun onComplete() {
        // 空实现 — 不在此处自动 dispose。
        // share() 上游在所有 subscriber dispose 后会触发 onComplete，
        // 如果此处也 dispose 会打断重连逻辑。
    }

    protected open fun onOpen(webSocket: WebSocketClient) {}
    protected open fun onMessage(webSocket: WebSocketClient, text: String) {}
    protected open fun onMessage(webSocket: WebSocketClient, bytes: ByteArray) {}
    protected open fun onClosed(code: Int, reason: String, remote: Boolean) {}
    protected open fun onError(exception: Exception) {}
}
