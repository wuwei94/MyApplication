package com.example.william.my.module.socket.activity.tcpsocket.netty

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.server.NettyServerService
import com.example.william.my.core.netty.client.NettyClient
import com.example.william.my.core.netty.client.NettyClientHandler
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.netty.server.NettyServerHandler
import com.example.william.my.module.socket.utils.NetworkUtils
import com.example.william.my.basic.basic_shared.R as SharedR

/**
 * Netty — 高性能网络框架（TCP Socket 客户端）
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
 *         override fun onOpen(host: String, port: Int) {
 *             // 连接成功
 *         }
 *         override fun onMessage(message: String) {
 *             // 收到消息
 *         }
 *         override fun onClosed(reason: String) {
 *             // 连接关闭
 *         }
 *         override fun onError(exception: Exception) {
 *             // 发生错误
 *         }
 *     }
 * )
 *
 * // 发送消息
 * NettyClient.send("Hello")
 *
 * // 关闭连接
 * NettyClient.disconnect()
 * ```
 *
 * 服务端日志展示方式：
 * - 内置服务端运行在进程内 Service 中，主控制台按真实时间合并展示客户端与服务端两侧日志；
 * - 页面在 onStart/onStop 通过 [NettyServer]（进程级单例）订阅服务端事件，
 *   事件以「【服务端】前缀 + 强调色」追加进同一控制台，与服务端收发时序一一对应。
 *
 * 适用场景：
 * - 高并发网络应用
 * - 实时通信
 * - 游戏服务器
 * - 即时通讯
 *
 * https://github.com/netty/netty
 */
@Route(path = RouterPath.Socket.NettyTcpSocketClient)
class NettyTcpSocketClientActivity : BasicResponseActivity() {

    @JvmField
    @Autowired
    var nettyServerService: NettyServerService? = null

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5567
    private val serverUrl: String get() = "$host:$port"

    /**
     * 事件类型 → 控制台颜色（均为深色控制台背景上可读的颜色）：
     * - 生命周期（启动/停止）：强调色 Sky #38BDF8
     * - 建立连接（客户端接入）：成功绿 #5FB84F
     * - 断开连接（客户端断开）：主题红 #FF4A26（与异常一致）
     * - 业务收发（收到消息）：警示黄 #FFE14D
     * - 异常错误：主题红 #FF4A26
     */
    private val colorState: Int by lazy { ContextCompat.getColor(this, SharedR.color.shared_color_console_accent) }
    private val colorConnect: Int by lazy { ContextCompat.getColor(this, SharedR.color.shared_color_success) }
    private val colorMessage: Int by lazy { ContextCompat.getColor(this, SharedR.color.shared_color_primary_special1) }
    private val colorError: Int by lazy { ContextCompat.getColor(this, SharedR.color.shared_color_accent) }

    /**
     * 服务端事件订阅：内置服务端运行在进程内 Service 中（NettyServer 为进程级单例），
     * 页面在 onStart/onStop 订阅/退订，事件合并进主控制台展示。
     */
    private val serverLogListener = object : NettyServerHandler.OnMessageListener {
        override fun onStarted(port: Int) {
            appendServerLog("已启动，监听端口：$port", colorState)
        }

        override fun onStopped() {
            appendServerLog("已停止", colorState)
        }

        override fun onClientConnected(remoteAddress: String) {
            appendServerLog("客户端连接：$remoteAddress", colorConnect)
        }

        override fun onClientDisconnected(remoteAddress: String) {
            appendServerLog("客户端断开：$remoteAddress", colorError)
        }

        override fun onMessage(remoteAddress: String, message: String) {
            appendServerLog("收到 $remoteAddress：$message", colorMessage)
        }

        override fun onError(remoteAddress: String, throwable: Throwable) {
            appendServerLog("${if (remoteAddress.isEmpty()) "" else "$remoteAddress "}${throwable.message}", colorError)
        }
    }

    /**
     * 以指定类型颜色追加一行服务端日志（自动带【服务端】前缀与时间戳）。
     */
    private fun appendServerLog(message: String, color: Int) {
        appendLog("【服务端】$message", color)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Netty TCP】客户端示例\n地址：$serverUrl\n需要先启动本地服务端")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "启动服务端（Start Server）",
        "停止服务端（Stop Server）",
        "连接服务器（Connect）",
        "发送消息（Send Message）",
        "断开连接（Disconnect）",
    )

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

    override fun onStart() {
        super.onStart()
        NettyServer.addListener(serverLogListener)
    }

    override fun onStop() {
        super.onStop()
        NettyServer.removeListener(serverLogListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        NettyClient.disconnect()
        nettyServerService?.stopServer(this)
    }

    private fun startServer() {
        nettyServerService?.startServer(this)
        // 启动为异步操作，真实结果由服务端事件（【服务端】已启动）写入控制台
        appendLog("【操作】已发起启动服务端，等待就绪...")
    }

    private fun stopServer() {
        nettyServerService?.stopServer(this)
        // 停止结果由服务端事件（【服务端】已停止）写入控制台
        appendLog("【操作】已发起停止服务端...")
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
                },
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
