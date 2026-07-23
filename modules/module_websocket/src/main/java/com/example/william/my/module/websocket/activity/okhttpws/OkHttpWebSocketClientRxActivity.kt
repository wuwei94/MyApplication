package com.example.william.my.module.websocket.activity.okhttpws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketObserver
import com.example.william.my.core.okhttpws.client.OkHttpWebSocketClientRx
import okhttp3.WebSocket
import okio.ByteString

/**
 * OkHttp WebSocket RxJava 封装示例
 * 通过 OkHttpWebSocketClientRx + OkHttpWebSocketObserver 使用
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocket.OkHttpWebSocketClientRx)
class OkHttpWebSocketClientRxActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("OkHttp WebSocket RxJava 封装\n\n点击下方按钮连接 WebSocket 服务器")
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
        OkHttpWebSocketClientRx
            .createWebSocket(Constants.Url_WebSocket)
            .subscribe(object : OkHttpWebSocketObserver() {
                override fun onOpen(webSocket: WebSocket) {
                    super.onOpen(webSocket)
                    appendLog("onOpen")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    webSocket.send("heart")  // 发送心跳保活
                    appendLog("onMessageString: $text")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    appendLog("onMessageByteString: $bytes")
                }

                override fun onClosed(code: Int, reason: String) {
                    super.onClosed(code, reason)
                    appendLog("onClosed: code=$code reason=$reason")
                }
            })
    }

    private fun disconnect() {
        OkHttpWebSocketClientRx.cancel(Constants.Url_WebSocket)
        appendLog("已断开连接")
    }
}
