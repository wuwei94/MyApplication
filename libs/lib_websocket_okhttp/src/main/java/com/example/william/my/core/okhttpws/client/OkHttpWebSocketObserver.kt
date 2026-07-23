package com.example.william.my.core.okhttpws.client

import com.example.william.my.core.okhttpws.OkHttpWebSocketInfo
import com.example.william.my.core.okhttpws.OkHttpWebSocketLogger
import io.reactivex.rxjava3.observers.DisposableObserver
import okhttp3.WebSocket
import okio.ByteString

abstract class OkHttpWebSocketObserver : DisposableObserver<OkHttpWebSocketInfo>() {

    override fun onNext(info: OkHttpWebSocketInfo) {
        when (info) {
            is OkHttpWebSocketInfo.Open -> onOpen(info.webSocket)
            is OkHttpWebSocketInfo.TextMessage -> onMessage(info.webSocket, info.text)
            is OkHttpWebSocketInfo.BytesMessage -> onMessage(info.webSocket, info.bytes)
            is OkHttpWebSocketInfo.Closed -> onClosed(info.code, info.reason)
        }
    }

    override fun onError(e: Throwable) {
        OkHttpWebSocketLogger.error("WebSocket error", e)
    }

    override fun onComplete() {
        // 空实现 — 不在此处自动 dispose。
        // share() 上游在所有 subscriber dispose 后会触发 onComplete，
        // 如果此处也 dispose 会打断重连逻辑。
    }

    protected open fun onOpen(webSocket: WebSocket) {}
    protected open fun onMessage(webSocket: WebSocket, text: String) {}
    protected open fun onMessage(webSocket: WebSocket, bytes: ByteString) {}
    protected open fun onClosed(code: Int, reason: String) {}
}
