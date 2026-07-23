package com.example.william.my.core.okhttpws.client

import com.example.william.my.core.okhttpws.OkHttpWebSocketInfo
import com.example.william.my.core.okhttpws.OkHttpWebSocketLogger
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class OkHttpWebSocketOnSubscribe(
    private val url: String,
    private val request: Request,
    private val okHttpClient: OkHttpClient
) : ObservableOnSubscribe<OkHttpWebSocketInfo> {

    override fun subscribe(emitter: ObservableEmitter<OkHttpWebSocketInfo>) {
        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                OkHttpWebSocketClientRx.setWebSocket(url, webSocket)
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpWebSocketInfo.Open(webSocket))
                }
                OkHttpWebSocketLogger.debug("onOpen: $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpWebSocketInfo.TextMessage(webSocket, text))
                }
                OkHttpWebSocketLogger.debug("onMessageString: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpWebSocketInfo.BytesMessage(webSocket, bytes))
                }
                OkHttpWebSocketLogger.debug("onMessageByteString: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                OkHttpWebSocketLogger.debug("onClosing: code=$code reason=$reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpWebSocketInfo.Closed(code, reason))
                }
                OkHttpWebSocketLogger.debug("onClosed: code=$code reason=$reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                OkHttpWebSocketLogger.debug("onFailure: ${t.message}")
                response?.let {
                    OkHttpWebSocketLogger.debug("onFailure code: ${it.code}")
                    OkHttpWebSocketLogger.debug("onFailure body: ${it.body?.string()}")
                }
                if (!emitter.isDisposed) {
                    emitter.onError(t)
                }
            }
        })
    }
}
