package com.example.william.my.module.websocket.activity.netty

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.netty.client.NettyClient
import com.example.william.my.core.netty.client.NettyClientHandler
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.module.websocket.service.NettyWebSocketServerService
import com.example.william.my.module.websocket.utils.NetworkUtils

/**
 * Netty TCP 客户端示例
 *
 * 演示使用 Netty 作为 TCP 客户端进行通信
 * 注意：Netty 是 TCP 协议，不是 WebSocket 协议
 * 需要先启动本地服务端
 */
@Route(path = RouterPath.WebSocket.NettyWebSocket.NettyWebSocketClient)
class NettyWebSocketClientActivity : BasicResponseActivity() {

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5567
    private val serverUrl: String get() = "$host:$port"
    private var serverStarted = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("【Netty TCP】客户端示例\n地址：$serverUrl\n需要先启动本地服务端")
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
        NettyClient.disconnect()
        if (serverStarted) {
            NettyWebSocketServerService.stopService(this)
        }
    }

    private fun startServer() {
        NettyWebSocketServerService.startService(this)
        serverStarted = true
        appendLog("【服务端】已启动，地址：$serverUrl")
    }

    private fun broadcastMessage() {
        if (!NettyServer.isRunning()) {
            appendLog("【状态】服务端未运行，无法广播")
            return
        }

        val message = "Hello from Server!"
        NettyServer.broadcast(message)
        appendLog("【广播】已发送：$message")
    }

    private fun stopServer() {
        if (!serverStarted) {
            appendLog("【状态】服务端未启动")
            return
        }
        NettyWebSocketServerService.stopService(this)
        serverStarted = false
        appendLog("【服务端】已停止")
    }

    private fun connect() {
        if (!NettyServer.isRunning()) {
            appendLog("【状态】服务端未启动，请先启动服务端")
            return
        }

        appendLog("【连接】正在连接 $serverUrl ...")
        Thread {
            NettyClient.connect(
                host = host,
                port = port,
                listener = object : NettyClientHandler.OnMessageListener {
                    override fun onConnected(remoteAddress: String) {
                        runOnUiThread {
                            appendLog("【连接】已连接到 $remoteAddress")
                            NettyClient.sendMessage("heart")
                        }
                    }

                    override fun onMessage(message: String) {
                        runOnUiThread {
                            appendLog("【消息】收到：$message")
                        }
                    }

                    override fun onDisconnected() {
                        runOnUiThread {
                            appendLog("【关闭】已断开连接")
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        runOnUiThread {
                            appendLog("【错误】${throwable.message}")
                        }
                    }
                }
            )
        }.start()
    }

    private fun sendMessage() {
        if (!NettyClient.isConnected()) {
            appendLog("【状态】未连接，无法发送消息")
            return
        }

        val message = "Hello from Client!"
        val success = NettyClient.sendMessage(message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        if (!NettyClient.isConnected()) {
            appendLog("【状态】未连接")
            return
        }

        NettyClient.disconnect()
        appendLog("【断开】已断开连接")
    }
}
