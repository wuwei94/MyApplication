package com.example.william.my.module.websocket.activity.okhttpws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

/**
 * OkHttp WebSocket 原始 API 示例
 * 直接使用 OkHttpClient.newWebSocket() + WebSocketListener 回调
 */
@Route(path = RouterPath.WebSocket.OkHttpWebSocket.OkHttpWebSocketClient)
class OkHttpWebSocketClientActivity : BasicResponseActivity() {

    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mWebSocket: WebSocket? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("OkHttp WebSocket 原始 API\n\n点击下方按钮连接 WebSocket 服务器")
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
        val request: Request = Request.Builder()
            .url(Constants.Url_WebSocket)
            .build()
        mWebSocket = mOkHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                appendLog("onOpen: ${response.code}")
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

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                appendLog("onClosing: code=$code reason=$reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                appendLog("onClosed: code=$code reason=$reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                val builder = StringBuilder("onFailure: ")
                if (t.message != null) {
                    builder.append("Throwable: ").append(t.message)
                }
                if (response != null) {
                    builder.append(" code=").append(response.code)
                    builder.append(" body=").append(Gson().toJson(response.body))
                }
                appendLog(builder.toString())
            }
        })
    }

    private fun disconnect() {
        mWebSocket?.cancel()
        mWebSocket = null
        appendLog("已断开连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        mWebSocket?.cancel()  // 页面销毁时断开连接，防止泄漏
    }
}
