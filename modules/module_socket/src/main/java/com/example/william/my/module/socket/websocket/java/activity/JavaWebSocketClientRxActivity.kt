package com.example.william.my.module.socket.websocket.java.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.client.JavaWebSocketClientRx
import com.example.william.my.core.javaws.client.JavaWebSocketRxObserver
import org.java_websocket.client.WebSocketClient

/**
 * Java-WebSocket + RxJava — Observable 事件流消费
 *
 * 演示使用 JavaWebSocketClientRx + JavaWebSocketRxObserver 进行 WebSocket 通信，
 * 将生命周期回调桥接为 RxJava 事件流，由 Observer 统一消费。
 *
 * 核心机制与避坑点：
 * 1. Observable 桥接：createWebSocket 返回可订阅的事件源
 * 2. 观察者封装：JavaWebSocketRxObserver 收敛 open/message/closed/error
 * 3. 统一释放：页面销毁时 cancel(url) 断开连接
 * 4. 轻量实现：不依赖 OkHttp 栈，库内自包含客户端能力
 *
 * 官方参考：
 * https://github.com/TooTallNate/Java-WebSocket
 */
@Route(path = RouterPath.Socket.JavaWebSocketClientRx)
class JavaWebSocketClientRxActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[Java-WebSocket]RxJava 封装\n地址：$serverUrl\n" +
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
        JavaWebSocketClientRx.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("[连接]正在连接 $serverUrl ...")
        JavaWebSocketClientRx
            .createWebSocket(serverUrl)
            .subscribe(object : JavaWebSocketRxObserver() {
                override fun onOpen(webSocket: WebSocketClient) {
                    appendLogAccent("[连接]已连接")
                }

                override fun onMessage(webSocket: WebSocketClient, text: String) {
                    appendLogAccent("[消息]收到：$text")
                }

                override fun onClosed(code: Int, reason: String, remote: Boolean) {
                    appendLogAccent("[关闭]已关闭：code=$code reason=$reason")
                }

                override fun onError(exception: Exception) {
                    appendLogAccent("✗ ${exception.message}")
                }
            })
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = JavaWebSocketClientRx.send(serverUrl, message)
        if (success) {
            appendLog("[发送]$message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun disconnect() {
        JavaWebSocketClientRx.close(serverUrl)
        appendLog("[断开]已断开连接")
    }
}
