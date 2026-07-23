package com.example.william.my.module.websocket.activity.javaws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.javaws.client.JavaWebSocketRxObserver
import com.example.william.my.core.javaws.client.JavaWebSocketClientRx
import org.java_websocket.client.WebSocketClient

/**
 * Java-WebSocket RxJava 封装示例
 * 通过 JavaWebSocketClientRx + JavaWebSocketRxObserver 使用
 */
@Route(path = RouterPath.WebSocket.JavaWebSocket.JavaWebSocketClientRx)
class JavaWebSocketClientRxActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("Java-WebSocket RxJava 封装\n\n点击下方按钮连接 WebSocket 服务器")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "连接 WebSocket",
            "断开 WebSocket"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> connect()
            1 -> disconnect()
        }
    }

    private fun connect() {
        appendLog("正在连接 ${Constants.Url_WebSocket} ...")
        JavaWebSocketClientRx
            .createWebSocket(Constants.Url_WebSocket)
            .subscribe(object : JavaWebSocketRxObserver() {

                override fun onOpen(webSocket: WebSocketClient) {
                    appendLog("onOpen")
                    webSocket.send("heart")  // 发送心跳保活
                }

                override fun onMessage(webSocket: WebSocketClient, text: String) {
                    appendLog("onMessageString: $text")
                }

                override fun onMessage(webSocket: WebSocketClient, bytes: ByteArray) {
                    appendLog("onMessageBytes: ${bytes.size} bytes")
                }

                override fun onClosed(code: Int, reason: String, remote: Boolean) {
                    appendLog("onClosed: code=$code reason=$reason remote=$remote")
                }

                override fun onError(exception: Exception) {
                    appendLog("onError: ${exception.message}")
                }
            })
    }

    private fun disconnect() {
        JavaWebSocketClientRx.close(Constants.Url_WebSocket)
        appendLog("已断开连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketClientRx.close(Constants.Url_WebSocket)  // 页面销毁时断开连接，防止泄漏
    }
}
