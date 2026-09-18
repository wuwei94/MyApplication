package com.example.william.my.module.socket.websocket.okhttp.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClient
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientListener
import okhttp3.Response
import okhttp3.WebSocket

/**
 * OkHttp WebSocket — 基于 OkHttp 的全双工实时通信客户端
 *
 * 核心机制与避坑点：
 * 1. HTTP Upgrade：一次握手后升级为全双工帧通信，服务器可主动推送
 * 2. 封装客户端：OkHttpWebSocketClient 统一 connect/send/cancel，复用 OkHttp 连接池
 * 3. 监听回调：OkHttpWebSocketClientListener 在 OkHttp 线程回报，UI 更新须切主线程
 * 4. 下行监听：onMessage 随 connect 挂接，无需单独注册
 * 5. 会话释放：页面销毁前 cancel(url)，避免长连接与线程驻留
 *
 * 官方参考：
 * https://square.github.io/okhttp/features/websockets
 */
@Route(path = RouterPath.Socket.OkHttpWebSocketClient)
class OkHttpWebSocketClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[OkHttp WebSocket]Listener 版\n地址：$serverUrl\n" +
                "覆盖建立连接 / 上行发送 / 应用层心跳 / 下行监听 / 关闭注销",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 连接服务器（Connect + 下行监听）",
        "2. 发送消息（Send Message）",
        "3. 发送应用层心跳（Heartbeat）",
        "4. 断开连接（Disconnect）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> sendMessage()
            2 -> sendHeartbeat()
            3 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpWebSocketClient.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("→ [连接] 正在连接 $serverUrl ...")
        OkHttpWebSocketClient.connect(
            url = serverUrl,
            listener = object : OkHttpWebSocketClientListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    runOnUiThread {
                        appendLog("✓ [连接] 已连接，下行监听 onMessage 已挂接")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    runOnUiThread {
                        appendLog("✓ [下行] 收到：$text")
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    runOnUiThread {
                        appendLog("✓ [关闭] 已关闭：code=$code reason=$reason")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    runOnUiThread {
                        appendLog("✗ ${t.message}")
                    }
                }
            },
        )
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        appendLog("→ [上行] 发送消息...")
        val success = OkHttpWebSocketClient.send(serverUrl, message)
        if (success) {
            appendLog("✓ [上行] $message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    /**
     * 应用层心跳：复用 send 通道发送探测帧，对照协议层 ping 由 OkHttp 连接池维护。
     */
    private fun sendHeartbeat() {
        val heartbeat = "ping ${System.currentTimeMillis()}"
        appendLog("→ [心跳] 发送应用层探测帧...")
        val success = OkHttpWebSocketClient.send(serverUrl, heartbeat)
        if (success) {
            appendLog("✓ [心跳] $heartbeat")
        } else {
            appendLog("✗ 心跳发送失败，请先连接")
        }
    }

    private fun disconnect() {
        OkHttpWebSocketClient.close(serverUrl)
        appendLog("✓ [断开] 已断开连接")
    }
}
