package com.example.william.my.core.javaws.client

import com.example.william.my.core.javaws.JavaWebSocketInfo
import com.example.william.my.core.javaws.JavaWebSocketLogger
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

/**
 * Java-WebSocket 客户端 RxJava 连接订阅
 */
class JavaWebSocketRxOnSubscribe(
    private val uri: URI,
    private val autoReconnect: Boolean,
    private val reconnectInterval: Long,
) : ObservableOnSubscribe<JavaWebSocketInfo> {

    override fun subscribe(emitter: ObservableEmitter<JavaWebSocketInfo>) {
        val client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                if (!emitter.isDisposed) {
                    emitter.onNext(JavaWebSocketInfo.Open(this))
                }
                JavaWebSocketLogger.debug("onOpen: $uri")
            }

            override fun onMessage(message: String) {
                if (!emitter.isDisposed) {
                    emitter.onNext(JavaWebSocketInfo.TextMessage(this, message))
                }
                JavaWebSocketLogger.debug("onMessageString: $message")
            }

            override fun onMessage(message: ByteBuffer) {
                if (!emitter.isDisposed) {
                    emitter.onNext(JavaWebSocketInfo.BytesMessage(this, message.array()))
                }
                JavaWebSocketLogger.debug("onMessageBytes")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (!emitter.isDisposed) {
                    emitter.onNext(JavaWebSocketInfo.Closed(code, reason ?: "", remote))
                }
                JavaWebSocketLogger.debug("onClose: code=$code reason=$reason remote=$remote")
            }

            override fun onError(ex: Exception) {
                JavaWebSocketLogger.debug("onError: ${ex.message}")
                if (!emitter.isDisposed) {
                    emitter.onError(ex)
                }
            }
        }

        JavaWebSocketClientRx.setWebSocket(uri.toString(), client)
        client.connect()
    }
}
