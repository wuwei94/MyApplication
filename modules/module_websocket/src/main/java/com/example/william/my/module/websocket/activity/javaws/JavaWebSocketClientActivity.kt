package com.example.william.my.module.websocket.activity.javaws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.client.JavaWebSocketClient
import com.example.william.my.core.javaws.client.JavaWebSocketClientListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

/**
 * Java-WebSocket 原始 API 示例
 * 直接使用 JavaWebSocketClient + JavaWebSocketClientListener 回调
 * https://github.com/TooTallNate/Java-WebSocket
 */
@Route(path = RouterPath.WebSocket.JavaWebSocket.JavaWebSocketClient)
class JavaWebSocketClientActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("Java-WebSocket 原始 API\n\n点击下方按钮连接 WebSocket 服务器")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "连接 WebSocket",
            "断开 WebSocket"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> connect()
            1 -> disconnect()
        }
    }

    private fun connect() {
        appendLog("正在连接 ${Constants.Url_WebSocket} ...")
        JavaWebSocketClient.connect(
            url = Constants.Url_WebSocket,
            autoReconnect = true,        // 断线自动重连
            reconnectInterval = 3000,    // 重连间隔 3 秒
            listener = object : JavaWebSocketClientListener() {

                override fun onOpen(webSocket: WebSocketClient, handshakedata: ServerHandshake) {
                    appendLog("onOpen: ${handshakedata.httpStatus}")
                    webSocket.send("heart")  // 发送心跳保活
                }

                override fun onMessage(webSocket: WebSocketClient, message: String) {
                    appendLog("onMessageString: $message")
                }

                override fun onClose(webSocket: WebSocketClient, code: Int, reason: String?, remote: Boolean) {
                    appendLog("onClosed: code=$code reason=$reason remote=$remote")
                }

                override fun onError(webSocket: WebSocketClient, ex: Exception) {
                    appendLog("onError: ${ex.message}")
                }
            }
        )
    }

    private fun disconnect() {
        JavaWebSocketClient.close(Constants.Url_WebSocket)
        appendLog("已断开连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketClient.close(Constants.Url_WebSocket)  // 页面销毁时断开连接，防止泄漏
    }
}
