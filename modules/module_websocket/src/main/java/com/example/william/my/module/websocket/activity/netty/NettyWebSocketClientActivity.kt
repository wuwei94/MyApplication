package com.example.william.my.module.websocket.activity.netty

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.netty.client.NettyClient
import com.example.william.my.core.netty.client.NettyClientHandler
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.server.ServerManager
import com.example.william.my.module.websocket.utils.NetworkUtils

/**
 * Netty — 高性能网络框架
 *
 * Netty 是一个高性能的异步事件驱动的网络应用框架。
 *
 * 核心特性：
 * 1. 高性能：基于 NIO，支持高并发
 * 2. 异步事件驱动：非阻塞 I/O，性能优秀
 * 3. 丰富的协议支持：支持 TCP、UDP、HTTP、WebSocket 等
 * 4. 易于使用：API 简单，易于扩展
 *
 * 基本用法：
 * ```kotlin
 * // 连接服务器
 * NettyClient.connect(
 *     host = "192.168.1.100",
 *     port = 8080,
 *     listener = object : NettyClientHandler.OnMessageListener {
 *         override fun onConnected(remoteAddress: String) {
 *             // 连接成功
 *         }
 *         override fun onMessage(message: String) {
 *             // 收到消息
 *         }
 *         override fun onDisconnected() {
 *             // 连接断开
 *         }
 *         override fun onError(throwable: Throwable) {
 *             // 发生错误
 *         }
 *     }
 * )
 *
 * // 发送消息
 * NettyClient.sendMessage("Hello")
 *
 * // 断开连接
 * NettyClient.disconnect()
 * ```
 *
 * 适用场景：
 * - 高并发网络应用
 * - 实时通信
 * - 游戏服务器
 * - 即时通讯
 *
 * https://github.com/netty/netty
 */
@Route(path = RouterPath.WebSocket.NettyWebSocketClient)
class NettyWebSocketClientActivity : BasicResponseActivity() {

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5567
    private val serverUrl: String get() = "$host:$port"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Netty TCP】客户端示例\n地址：$serverUrl\n需要先启动本地服务端")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "启动服务端（Start Server）",
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
            1 -> stopServer()
            2 -> connect()
            3 -> sendMessage()
            4 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NettyClient.disconnect()
        ServerManager.stopNettyServer(this)
    }

    private fun startServer() {
        ServerManager.startNettyServer(this)
        appendLog("【服务端】已启动，地址：$serverUrl")
    }

    private fun stopServer() {
        ServerManager.stopNettyServer(this)
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
