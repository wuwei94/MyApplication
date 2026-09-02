package com.example.william.my.module.websocket.activity.netty

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.client.NettyClientFlow
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.server.ServerManager
import com.example.william.my.module.websocket.utils.NetworkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Netty Coroutines Flow 封装示例
 *
 * 演示使用 NettyClientFlow 进行 TCP 通信
 * 使用 Kotlin Coroutines Flow 收集 TCP 事件
 * 需要先启动本地服务端
 */
@Route(path = RouterPath.WebSocket.NettyWebSocketClientFlow)
class NettyWebSocketClientFlowActivity : BasicResponseActivity() {

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5567
    private val serverUrl: String get() = "$host:$port"
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Netty TCP】Coroutines Flow 封装\n地址：$serverUrl\n需要先启动本地服务端")
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
        connectJob?.cancel()
        NettyClientFlow.close(host, port)
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

        connectJob?.cancel()
        appendLog("【连接】正在连接 $serverUrl ...")
        connectJob = lifecycleScope.launch {
            NettyClientFlow
                .createConnection(host, port)
                .collect { info ->
                    when (info) {
                        is NettyClientInfo.Open -> {
                            appendLog("【连接】已连接到 ${info.host}:${info.port}")
                            NettyClientFlow.send(host, port, "heart")
                        }
                        is NettyClientInfo.TextMessage -> {
                            appendLog("【消息】收到：${info.message}")
                        }
                        is NettyClientInfo.Closed -> {
                            appendLog("【关闭】已关闭：${info.reason}")
                        }
                        is NettyClientInfo.Error -> {
                            appendLog("【错误】${info.exception.message}")
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val channel = NettyClientFlow.getChannel(host, port)
        if (channel == null || !channel.isActive) {
            appendLog("【状态】未连接，无法发送消息")
            return
        }

        val message = "Hello from Client (Flow)!"
        val success = NettyClientFlow.send(host, port, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        NettyClientFlow.close(host, port)
        appendLog("【断开】已断开连接")
    }
}
