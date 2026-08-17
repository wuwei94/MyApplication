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
 *
 * 演示使用 JavaWebSocketClient + JavaWebSocketClientListener 回调
 */
@Route(path = RouterPath.WebSocket.JavaWebSocketClient)
class JavaWebSocketClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Java-WebSocket】原始 API\n地址：$serverUrl")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "连接服务器（Connect）",
            "发送消息（Send Message）",
            "断开连接（Disconnect）",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> sendMessage()
            2 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketClient.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("【连接】正在连接 $serverUrl ...")
        JavaWebSocketClient.connect(
            url = serverUrl,
            autoReconnect = true,
            reconnectInterval = 3000,
            listener = object : JavaWebSocketClientListener() {
                override fun onOpen(webSocket: WebSocketClient, handshakedata: ServerHandshake) {
                    runOnUiThread {
                        appendLogAccent("【连接】已连接")
                    }
                }

                override fun onMessage(webSocket: WebSocketClient, message: String) {
                    runOnUiThread {
                        appendLogAccent("【消息】收到：$message")
                    }
                }

                override fun onClose(webSocket: WebSocketClient, code: Int, reason: String?, remote: Boolean) {
                    runOnUiThread {
                        appendLogAccent("【关闭】已关闭：code=$code reason=$reason")
                    }
                }

                override fun onError(webSocket: WebSocketClient, ex: Exception) {
                    runOnUiThread {
                        appendLogAccent("【错误】${ex.message}")
                    }
                }
            }
        )
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = JavaWebSocketClient.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        JavaWebSocketClient.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
