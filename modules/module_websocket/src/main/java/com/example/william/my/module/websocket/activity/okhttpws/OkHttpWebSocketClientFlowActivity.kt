package com.example.william.my.module.websocket.activity.okhttpws

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
 * OkHttp WebSocket Coroutines Flow 封装示例
 *
 * 演示使用 OkHttpWebSocketClientFlow 进行 WebSocket 通信
 * 使用 Kotlin Coroutines Flow 收集 WebSocket 事件
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocketClientFlow)
class OkHttpWebSocketClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket
    private var connectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp WebSocket】Coroutines Flow 封装\n地址：$serverUrl")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "连接服务器（Connect）",
            "发送消息（Send Message）",
            "断开连接（Disconnect）",
        )
    }

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
                            appendLogAccent("【错误】${info.exception.message}")
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
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        connectJob?.cancel()
        OkHttpWebSocketClientFlow.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
