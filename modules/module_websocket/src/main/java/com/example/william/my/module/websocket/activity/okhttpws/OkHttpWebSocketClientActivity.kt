package com.example.william.my.module.websocket.activity.okhttpws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClient
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientListener
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

/**
 * OkHttp WebSocket 客户端示例（普通版本）
 *
 * 演示使用 OkHttpWebSocketClient 封装进行 WebSocket 通信
 * 连接到 echo.websocket.org 服务器
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocket.OkHttpWebSocketClient)
class OkHttpWebSocketClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("【OkHttp WebSocket】普通版本\n地址：$serverUrl")
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
        OkHttpWebSocketClient.cancel(serverUrl)
    }

    private fun connect() {
        if (OkHttpWebSocketClient.isConnected(serverUrl)) {
            appendLog("【状态】已连接到服务器")
            return
        }

        appendLog("【连接】正在连接 $serverUrl ...")
        OkHttpWebSocketClient.connect(
            url = serverUrl,
            listener = object : OkHttpWebSocketClientListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    runOnUiThread {
                        appendLog("【连接】已连接，状态码：${response.code}")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    runOnUiThread {
                        appendLog("【消息】收到：$text")
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    runOnUiThread {
                        appendLog("【消息】收到字节：$bytes")
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    runOnUiThread {
                        appendLog("【关闭】正在关闭：code=$code reason=$reason")
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    runOnUiThread {
                        appendLog("【关闭】已关闭：code=$code reason=$reason")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    runOnUiThread {
                        appendLog("【错误】${t.message}")
                    }
                }
            }
        )
    }

    private fun sendMessage() {
        if (!OkHttpWebSocketClient.isConnected(serverUrl)) {
            appendLog("【状态】未连接，无法发送消息")
            return
        }

        val message = "Hello from Client!"
        val success = OkHttpWebSocketClient.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        if (!OkHttpWebSocketClient.isConnected(serverUrl)) {
            appendLog("【状态】未连接")
            return
        }

        OkHttpWebSocketClient.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
