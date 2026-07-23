package com.example.william.my.module.websocket.activity.javaws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.client.JavaWebSocketClientRx
import com.example.william.my.core.javaws.client.JavaWebSocketRxObserver
import com.example.william.my.core.javaws.server.JavaWebSocketServer
import com.example.william.my.module.websocket.service.JavaWebSocketServerService
import com.example.william.my.module.websocket.utils.NetworkUtils
import org.java_websocket.client.WebSocketClient

/**
 * Java-WebSocket RxJava 封装示例
 *
 * 演示使用 JavaWebSocketClientRx + JavaWebSocketRxObserver 进行 WebSocket 通信
 * 需要先启动本地服务端
 */
@Route(path = RouterPath.WebSocket.JavaWebSocket.JavaWebSocketClientRx)
class JavaWebSocketClientRxActivity : BasicResponseActivity() {

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5566
    private val serverUrl: String get() = "ws://$host:$port"
    private var serverStarted = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("【Java-WebSocket】RxJava 封装\n地址：$serverUrl\n需要先启动本地服务端")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "启动服务端（Start Server）",
            "广播消息（Broadcast Message）",
            "停止服务端（Stop Server）",
            "连接服务器（Connect）",
            "发送消息（Send Message）",
            "断开连接（Disconnect）",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> startServer()
            1 -> broadcastMessage()
            2 -> stopServer()
            3 -> connect()
            4 -> sendMessage()
            5 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketClientRx.close(serverUrl)
        if (serverStarted) {
            JavaWebSocketServerService.stopService(this)
        }
    }

    private fun startServer() {
        JavaWebSocketServerService.startService(this)
        serverStarted = true
        appendLog("【服务端】已启动，地址：$serverUrl")
    }

    private fun broadcastMessage() {
        if (!JavaWebSocketServer.isRunning()) {
            appendLog("【状态】服务端未运行，无法广播")
            return
        }

        val message = "Hello from Server!"
        JavaWebSocketServer.broadcast(message)
        appendLog("【广播】已发送：$message")
    }

    private fun stopServer() {
        if (!serverStarted) {
            appendLog("【状态】服务端未启动")
            return
        }
        JavaWebSocketServerService.stopService(this)
        serverStarted = false
        appendLog("【服务端】已停止")
    }

    private fun connect() {
        if (!JavaWebSocketServer.isRunning()) {
            appendLog("【状态】服务端未启动，请先启动服务端")
            return
        }

        appendLog("【连接】正在连接 $serverUrl ...")
        JavaWebSocketClientRx
            .createWebSocket(serverUrl)
            .subscribe(object : JavaWebSocketRxObserver() {
                override fun onOpen(webSocket: WebSocketClient) {
                    runOnUiThread {
                        appendLog("【连接】已连接")
                        webSocket.send("heart")
                    }
                }

                override fun onMessage(webSocket: WebSocketClient, text: String) {
                    runOnUiThread {
                        appendLog("【消息】收到：$text")
                    }
                }

                override fun onClosed(code: Int, reason: String, remote: Boolean) {
                    runOnUiThread {
                        appendLog("【关闭】已关闭：code=$code reason=$reason")
                    }
                }

                override fun onError(exception: Exception) {
                    runOnUiThread {
                        appendLog("【错误】${exception.message}")
                    }
                }
            })
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = JavaWebSocketClientRx.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        JavaWebSocketClientRx.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
