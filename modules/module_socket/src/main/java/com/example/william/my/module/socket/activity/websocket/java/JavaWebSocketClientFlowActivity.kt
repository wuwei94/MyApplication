package com.example.william.my.module.socket.activity.websocket.java

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.JavaWebSocketInfo
import com.example.william.my.core.javaws.client.JavaWebSocketClientFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Java-WebSocket + Flow — 协程流式事件消费
 *
 * 演示使用 JavaWebSocketClientFlow 进行 WebSocket 通信，
 * 将连接、消息、关闭、错误事件桥接为 Kotlin Flow，由 collect 逐条消费。
 *
 * 核心机制与避坑点：
 * 1. 冷流桥接：createWebSocket 返回可 collect 的事件流
 * 2. 事件密封：JavaWebSocketInfo 统一承载 Open/Message/Closed/Error
 * 3. 结构化取消：collect 所在 Job 取消即停止消费
 * 4. 轻量实现：不依赖 OkHttp 栈，库内自包含客户端能力
 *
 * 官方参考：
 * https://github.com/TooTallNate/Java-WebSocket
 */
@Route(path = RouterPath.Socket.JavaWebSocketClientFlow)
class JavaWebSocketClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[Java-WebSocket]Coroutines Flow 封装\n地址：$serverUrl\n" +
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
     * 输出当前库内自动重连参数与下行监听挂接方式。
     */
    private fun showReconnectPolicy() {
        appendLog("✓ [重连] autoReconnect=true，reconnectInterval=3000ms")
        appendLog("✓ [下行] onMessage 随连接建立挂接，无需单独注册")
    }

    override fun onDestroy() {
        super.onDestroy()
        connectJob?.cancel()
        JavaWebSocketClientFlow.cancel(serverUrl)
    }

    private fun connect() {
        connectJob?.cancel()
        appendLog("[连接]正在连接 $serverUrl ...")
        connectJob = lifecycleScope.launch {
            JavaWebSocketClientFlow
                .createWebSocket(serverUrl)
                .collect { info ->
                    when (info) {
                        is JavaWebSocketInfo.Open -> {
                            appendLogAccent("[连接]已连接")
                        }
                        is JavaWebSocketInfo.TextMessage -> {
                            appendLogAccent("[消息]收到：${info.message}")
                        }
                        is JavaWebSocketInfo.BytesMessage -> {
                            appendLogAccent("[消息]收到字节数据：${info.bytes.size} bytes")
                        }
                        is JavaWebSocketInfo.Closed -> {
                            appendLogAccent("[关闭]已关闭：code=${info.code} reason=${info.reason}")
                        }
                        is JavaWebSocketInfo.Error -> {
                            appendLogAccent("✗ ${info.exception.message}")
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val message = "Hello from Client (Flow)!"
        val success = JavaWebSocketClientFlow.send(serverUrl, message)
        if (success) {
            appendLog("[发送]$message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        JavaWebSocketClientFlow.close(serverUrl)
        appendLog("[断开]已断开连接")
    }
}
