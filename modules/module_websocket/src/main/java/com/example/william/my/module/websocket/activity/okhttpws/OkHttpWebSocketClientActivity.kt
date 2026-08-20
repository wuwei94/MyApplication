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

/**
 * OkHttp WebSocket 客户端示例（普通版本）
 *
 * WebSocket 是一种在单个 TCP 连接上进行全双工通信的协议，适合实时应用场景。
 *
 * 与 HTTP 的区别：
 * - HTTP：请求-响应模式，单向通信
 * - WebSocket：全双向通信，服务器可主动推送
 *
 * 典型应用场景：
 * - 实时聊天、即时通讯
 * - 在线游戏、实时数据推送
 * - 股票行情、实时监控
 *
 * 本示例使用 OkHttpWebSocketClient 封装进行 WebSocket 通信，
 * 演示连接、发送消息、断开连接的基本操作。
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocketClient)
class OkHttpWebSocketClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp WebSocket】普通版本\n地址：$serverUrl")
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
        appendLog("【连接】正在连接 $serverUrl ...")
        OkHttpWebSocketClient.connect(
            url = serverUrl,
            listener = object : OkHttpWebSocketClientListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    runOnUiThread {
                        appendLogAccent("【连接】已连接")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    runOnUiThread {
                        appendLogAccent("【消息】收到：$text")
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    runOnUiThread {
                        appendLogAccent("【关闭】已关闭：code=$code reason=$reason")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    runOnUiThread {
                        appendLogAccent("【错误】${t.message}")
                    }
                }
            }
        )
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = OkHttpWebSocketClient.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        OkHttpWebSocketClient.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
