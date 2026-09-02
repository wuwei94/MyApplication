package com.example.william.my.module.socket.activity.tcpsocket.netty

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.netty.client.NettyClientRx
import com.example.william.my.core.netty.client.NettyClientRxObserver
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.server.ServerManager
import com.example.william.my.module.socket.utils.NetworkUtils

/**
 * Netty RxJava 封装示例（TCP Socket）
 *
 * 演示使用 NettyClientRx + NettyClientRxObserver 进行 TCP 通信
 * 使用 RxJava Observable 方式处理事件
 * 需要先启动本地服务端
 */
@Route(path = RouterPath.Socket.NettyTcpSocketClientRx)
class NettyTcpSocketClientRxActivity : BasicResponseActivity() {

    private val host: String get() = NetworkUtils.getIPAddress(true)
    private val port: Int = 5567
    private val serverUrl: String get() = "$host:$port"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Netty TCP】RxJava 封装\n地址：$serverUrl\n需要先启动本地服务端")
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
        NettyClientRx.close(host, port)
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
        NettyClientRx
            .createConnection(host, port)
            .subscribe(object : NettyClientRxObserver() {
                override fun onConnected(host: String, port: Int) {
                    runOnUiThread {
                        appendLog("【连接】已连接到 $host:$port")
                        NettyClientRx.send(host, port, "heart")
                    }
                }

                override fun onMessage(message: String) {
                    runOnUiThread {
                        appendLog("【消息】收到：$message")
                    }
                }

                override fun onClosed(reason: String) {
                    runOnUiThread {
                        appendLog("【关闭】已关闭：$reason")
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
        val channel = NettyClientRx.getChannel(host, port)
        if (channel == null || !channel.isActive) {
            appendLog("【状态】未连接，无法发送消息")
            return
        }

        val message = "Hello from Client!"
        val success = NettyClientRx.send(host, port, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        NettyClientRx.close(host, port)
        appendLog("【断开】已断开连接")
    }
}
