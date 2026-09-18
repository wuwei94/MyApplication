package com.example.william.my.module.socket.websocket.okhttp.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientRx
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketObserver
import okhttp3.WebSocket

/**
 * OkHttp WebSocket + RxJava — Observable 事件流消费
 *
 * 演示使用 OkHttpWebSocketClientRx + OkHttpWebSocketObserver 进行 WebSocket 通信，
 * 将生命周期回调桥接为 RxJava 事件流，由 Observer 统一消费。
 *
 * 核心机制与避坑点：
 * 1. Observable 桥接：createWebSocket 返回可订阅的事件源
 * 2. 观察者封装：OkHttpWebSocketObserver 收敛 open/message/closed/error
 * 3. 下行监听：onMessage 随 subscribe 挂接
 * 4. 统一释放：页面销毁时 cancel(url) 断开连接
 * 5. 与回调版对齐：事件语义与普通监听器版本一致
 *
 * 官方参考：
 * https://square.github.io/okhttp/features/websockets
 */
@Route(path = RouterPath.Socket.OkHttpWebSocketClientRx)
class OkHttpWebSocketClientRxActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[OkHttp WebSocket]RxJava 封装\n地址：$serverUrl\n" +
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
        OkHttpWebSocketClientRx.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("→ [连接] 正在连接 $serverUrl ...")
        OkHttpWebSocketClientRx
            .createWebSocket(serverUrl)
            .subscribe(object : OkHttpWebSocketObserver() {
                override fun onOpen(webSocket: WebSocket) {
                    appendLog("✓ [连接] 已连接，下行监听 onMessage 已挂接")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    appendLog("✓ [下行] 收到：$text")
                }

                override fun onClosed(code: Int, reason: String) {
                    appendLog("✓ [关闭] 已关闭：code=$code reason=$reason")
                }

                override fun onError(exception: Exception) {
                    appendLog("✗ ${exception.message}")
                }
            })
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        appendLog("→ [上行] 发送消息...")
        val success = OkHttpWebSocketClientRx.send(serverUrl, message)
        if (success) {
            appendLog("✓ [上行] $message")
        } else {
            appendLog("✗ 发送失败")
        }
    }

    private fun sendHeartbeat() {
        val heartbeat = "ping ${System.currentTimeMillis()}"
        appendLog("→ [心跳] 发送应用层探测帧...")
        val success = OkHttpWebSocketClientRx.send(serverUrl, heartbeat)
        if (success) {
            appendLog("✓ [心跳] $heartbeat")
        } else {
            appendLog("✗ 心跳发送失败，请先连接")
        }
    }

    private fun disconnect() {
        OkHttpWebSocketClientRx.close(serverUrl)
        appendLog("✓ [断开] 已断开连接")
    }
}
