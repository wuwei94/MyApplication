package com.example.william.my.core.websocket

import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketOnSubscribe(
    private val url: String,
    private val request: Request,
    private val okHttpClient: OkHttpClient
) : ObservableOnSubscribe<WebSocketInfo> {

    override fun subscribe(emitter: ObservableEmitter<WebSocketInfo>) {
        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                WebSocketUtils.setWebSocket(url, webSocket)
                if (!emitter.isDisposed) {
                    emitter.onNext(WebSocketInfo.Open(webSocket))
                }
                WebSocketLogger.debug("onOpen: $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (!emitter.isDisposed) {
                    emitter.onNext(WebSocketInfo.TextMessage(webSocket, text))
                }
                WebSocketLogger.debug("onMessageString: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                if (!emitter.isDisposed) {
                    emitter.onNext(WebSocketInfo.BytesMessage(webSocket, bytes))
                }
                WebSocketLogger.debug("onMessageByteString: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                WebSocketLogger.debug("onClosing: code=$code reason=$reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!emitter.isDisposed) {
                    emitter.onNext(WebSocketInfo.Closed(code, reason))
                }
                WebSocketLogger.debug("onClosed: code=$code reason=$reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                WebSocketLogger.debug("onFailure: ${t.message}")
                response?.let {
                    WebSocketLogger.debug("onFailure code: ${it.code}")
                    WebSocketLogger.debug("onFailure body: ${it.body?.string()}")
                }
                if (!emitter.isDisposed) {
                    emitter.onError(t)
                }
            }
        })
    }
}
