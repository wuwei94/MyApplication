package com.example.william.my.module.websocket.activity.okhttpws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientRx
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketObserver
import okhttp3.WebSocket

/**
 * OkHttp WebSocket RxJava 封装示例
 *
 * 演示使用 OkHttpWebSocketClientRx + OkHttpWebSocketObserver 进行 WebSocket 通信
 * 连接到 echo.websocket.org 服务器
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocketClientRx)
class OkHttpWebSocketClientRxActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_WebSocket

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp WebSocket】RxJava 封装\n地址：$serverUrl")
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
        OkHttpWebSocketClientRx.cancel(serverUrl)
    }

    private fun connect() {
        appendLog("【连接】正在连接 $serverUrl ...")
        OkHttpWebSocketClientRx
            .createWebSocket(serverUrl)
            .subscribe(object : OkHttpWebSocketObserver() {
                override fun onOpen(webSocket: WebSocket) {
                    appendLogAccent("【连接】已连接")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    appendLogAccent("【消息】收到：$text")
                }

                override fun onClosed(code: Int, reason: String) {
                    appendLogAccent("【关闭】已关闭：code=$code reason=$reason")
                }

                override fun onError(exception: Exception) {
                    appendLogAccent("【错误】${exception.message}")
                }
            })
    }

    private fun sendMessage() {
        val message = "Hello from Client!"
        val success = OkHttpWebSocketClientRx.send(serverUrl, message)
        if (success) {
            appendLog("【发送】$message")
        } else {
            appendLog("【错误】发送失败")
        }
    }

    private fun disconnect() {
        OkHttpWebSocketClientRx.close(serverUrl)
        appendLog("【断开】已断开连接")
    }
}
