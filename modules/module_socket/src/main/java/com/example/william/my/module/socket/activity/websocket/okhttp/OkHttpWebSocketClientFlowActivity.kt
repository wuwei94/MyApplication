package com.example.william.my.module.socket.activity.websocket.okhttp

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
 * 3. 结构化取消：collect 所在 Job 取消即停止消费
 * 4. 主动断开：cancel(url) 关闭底层 WebSocket
 *
 * https://square.github.io/okhttp/features/websockets
 */
@Route(path = RouterPath.Socket.OkHttpWebSocketClientFlow)
class OkHttpWebSocketClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp WebSocket】Coroutines Flow 封装\n地址：$serverUrl")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "连接服务器（Connect）",
        "发送消息（Send Message）",
        "断开连接（Disconnect）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> sendMessage()
            2 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectJob?.cancel()
        OkHttpWebSocketClientFlow.cancel(serverUrl)
    }

    private fun connect() {
        connectJob?.cancel()
        appendLog("【连接】正在连接 $serverUrl ...")
        connectJob = lifecycleScope.launch {
            OkHttpWebSocketClientFlow
                .createWebSocket(serverUrl)
                .collect { info ->
                    when (info) {
                        is OkHttpWebSocketInfo.Open -> {
                            appendLogAccent("【连接】已连接")
                        }
                        is OkHttpWebSocketInfo.TextMessage -> {
                            appendLogAccent("【消息】收到：${info.text}")
                        }
                        is OkHttpWebSocketInfo.BytesMessage -> {
                            appendLogAccent("【消息】收到字节数据：${info.bytes.size} bytes")
                        }
                        is OkHttpWebSocketInfo.Closed -> {
                            appendLogAccent("【关闭】已关闭：code=${info.code} reason=${info.reason}")
                        }
                        is OkHttpWebSocketInfo.Error -> {
                            appendLogAccent("✗ ${info.exception.message}")
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val message = "Hello from Client (Flow)!"
        val success = OkHttpWebSocketClientFlow.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        OkHttpWebSocketClientFlow.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
