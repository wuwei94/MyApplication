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
 * Java-WebSocket Coroutines Flow 封装示例
 *
 * 演示使用 JavaWebSocketClientFlow 进行 WebSocket 通信
 * 使用 Kotlin Coroutines Flow 收集 WebSocket 事件
 */
@Route(path = RouterPath.Socket.JavaWebSocketClientFlow)
class JavaWebSocketClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Java-WebSocket】Coroutines Flow 封装\n地址：$serverUrl")
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
        JavaWebSocketClientFlow.cancel(serverUrl)
    }

    private fun connect() {
        connectJob?.cancel()
        appendLog("【连接】正在连接 $serverUrl ...")
        connectJob = lifecycleScope.launch {
            JavaWebSocketClientFlow
                .createWebSocket(serverUrl)
                .collect { info ->
                    when (info) {
                        is JavaWebSocketInfo.Open -> {
                            appendLogAccent("【连接】已连接")
                        }
                        is JavaWebSocketInfo.TextMessage -> {
                            appendLogAccent("【消息】收到：${info.message}")
                        }
                        is JavaWebSocketInfo.BytesMessage -> {
                            appendLogAccent("【消息】收到字节数据：${info.bytes.size} bytes")
                        }
                        is JavaWebSocketInfo.Closed -> {
                            appendLogAccent("【关闭】已关闭：code=${info.code} reason=${info.reason}")
                        }
                        is JavaWebSocketInfo.Error -> {
                            appendLogAccent("【错误】${info.exception.message}")
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val message = "Hello from Client (Flow)!"
        val success = JavaWebSocketClientFlow.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        JavaWebSocketClientFlow.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
