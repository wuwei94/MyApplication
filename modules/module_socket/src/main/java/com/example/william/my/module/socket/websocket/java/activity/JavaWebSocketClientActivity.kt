package com.example.william.my.module.socket.websocket.java.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.client.JavaWebSocketClient
import com.example.william.my.core.javaws.client.JavaWebSocketClientListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

/**
 * Java-WebSocket — Java WebSocket 客户端
 *
 * 核心机制与避坑点：
 * 1. 独立客户端栈：不依赖 OkHttp，库内自包含连接与收发能力
 * 2. 回调线程：onOpen/onMessage/onClose/onError 在库内部线程，更新 UI 须切主线程
 * 3. 连接生命周期：connect 后须在页面销毁前 close，避免 socket 与线程残留
 * 4. 与 OkHttp WS 平行：适合不想引入 OkHttp 的轻量项目，语义对照见 OkHttp 兄弟页
 *
 * 官方参考：
 * https://github.com/TooTallNate/Java-WebSocket
 */
@Route(path = RouterPath.Socket.JavaWebSocketClient)
class JavaWebSocketClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[Java-WebSocket]Listener 版\n地址：$serverUrl\n" +
                "覆盖建立连接 / 上行发送 / 下行监听 / 断线自动重连 / 关闭注销",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 连接服务器（Connect + 下行监听）",
        "2. 发送消息（Send Message）",
        "3. 断线自动重连策略说明（Reconnect）",
        "4. 断开连接（Disconnect）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> sendMessage()
            2 -> showReconnectPolicy()
            3 -> disconnect()
        }
    }

    /**
     * 库内 connect 已配置 autoReconnect / reconnectInterval，本项输出当前策略。
     */
    private fun showReconnectPolicy() {
        appendLog("✓ [重连] JavaWebSocketClient.connect 默认 autoReconnect=true")
        appendLog("✓ [重连] reconnectInterval=3000ms，close 时 cancelReconnect 停止调度")
        appendLog("✓ [下行] onMessage 随 connect 挂接，无需单独注册")
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketClient.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("[连接]正在连接 $serverUrl ...")
        JavaWebSocketClient.connect(
            url = serverUrl,
            autoReconnect = true,
            reconnectInterval = 3000,
            listener = object : JavaWebSocketClientListener() {
                override fun onOpen(webSocket: WebSocketClient, handshakedata: ServerHandshake) {
                    runOnUiThread {
                        appendLogAccent("[连接]已连接")
                    }
                }

                override fun onMessage(webSocket: WebSocketClient, message: String) {
                    runOnUiThread {
                        appendLogAccent("[消息]收到：$message")
                    }
                }

                override fun onClose(webSocket: WebSocketClient, code: Int, reason: String?, remote: Boolean) {
                    runOnUiThread {
                        appendLogAccent("[关闭]已关闭：code=$code reason=$reason")
                    }
                }

                override fun onError(webSocket: WebSocketClient, ex: Exception) {
                    runOnUiThread {
                        appendLogAccent("✗ ${ex.message}")
                    }
                }
            },
        )
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = JavaWebSocketClient.send(serverUrl, message)
        if (success) {
            appendLog("[发送]$message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun disconnect() {
        JavaWebSocketClient.close(serverUrl)
        appendLog("[断开]已断开连接")
    }
}
