package com.example.william.my.module.socket.websocket.okhttp.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.OkHttpWebSocketInfo
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * OkHttp WebSocket + Flow — 协程流式事件消费
 *
 * 演示使用 OkHttpWebSocketClientFlow 进行 WebSocket 通信，
 * 将连接、消息、关闭、错误事件桥接为 Kotlin Flow，由 collect 逐条消费。
 *
 * 核心机制与避坑点：
 * 1. 冷流桥接：createWebSocket 返回可 collect 的事件流
 * 2. 事件密封：OkHttpWebSocketInfo 统一承载 Open/Message/Closed/Error
 * 3. 下行监听：TextMessage/BytesMessage 随 collect 消费
 * 4. 结构化取消：collect 所在 Job 取消即停止消费
 * 5. 主动断开：cancel(url) 关闭底层 WebSocket
 *
 * 官方参考：
 * https://square.github.io/okhttp/features/websockets
 */
@Route(path = RouterPath.Socket.OkHttpWebSocketClientFlow)
class OkHttpWebSocketClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[OkHttp WebSocket]Coroutines Flow 封装\n地址：$serverUrl\n" +
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
        connectJob?.cancel()
        OkHttpWebSocketClientFlow.cancel(serverUrl)
    }

    private fun connect() {
        connectJob?.cancel()
        appendLog("→ [连接] 正在连接 $serverUrl ...")
        connectJob = lifecycleScope.launch {
            OkHttpWebSocketClientFlow
                .createWebSocket(serverUrl)
                .collect { info ->
                    when (info) {
                        is OkHttpWebSocketInfo.Open -> {
                            appendLog("✓ [连接] 已连接，下行监听随 collect 挂接")
                        }
                        is OkHttpWebSocketInfo.TextMessage -> {
                            appendLog("✓ [下行] 收到：${info.text}")
                        }
                        is OkHttpWebSocketInfo.BytesMessage -> {
                            appendLog("✓ [下行] 收到字节数据：${info.bytes.size} bytes")
                        }
                        is OkHttpWebSocketInfo.Closed -> {
                            appendLog("✓ [关闭] 已关闭：code=${info.code} reason=${info.reason}")
                        }
                        is OkHttpWebSocketInfo.Error -> {
                            appendLog("✗ ${info.exception.message}")
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val message = "Hello from Client (Flow)!"
        appendLog("→ [上行] 发送消息...")
        val success = OkHttpWebSocketClientFlow.send(serverUrl, message)
        if (success) {
            appendLog("✓ [上行] $message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun sendHeartbeat() {
        val heartbeat = "ping ${System.currentTimeMillis()}"
        appendLog("→ [心跳] 发送应用层探测帧...")
        val success = OkHttpWebSocketClientFlow.send(serverUrl, heartbeat)
        if (success) {
            appendLog("✓ [心跳] $heartbeat")
        } else {
            appendLog("✗ 心跳发送失败，请先连接")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        OkHttpWebSocketClientFlow.close(serverUrl)
        appendLog("✓ [断开] 已断开连接")
    }
}
