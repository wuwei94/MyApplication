package com.example.william.my.module.websocket.activity.javaws

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.websocket.service.JavaWebSocketServerService
import com.example.william.my.module.websocket.utils.NetworkUtils

/**
 * Java-WebSocket 服务端示例
 * 通过 JavaWebSocketServerService 启动本地 WebSocket 服务器
 * https://github.com/TooTallNate/Java-WebSocket
 */
@Route(path = RouterPath.WebSocket.JavaWebSocket.JavaWebSocketServerService)
class JavaWebSocketServiceActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("Java-WebSocket 服务端\n\n点击下方按钮启动/停止 WebSocket 服务器")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "启动服务",
            "停止服务"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startServer()
            1 -> stopServer()
        }
    }

    private fun startServer() {
        val ip = NetworkUtils.getIPAddress(true)
        appendLog("启动 WebSocket 服务器")
        appendLog("连接地址: ws://$ip:5566")
        JavaWebSocketServerService.startService(this)
    }

    private fun stopServer() {
        JavaWebSocketServerService.stopService(this)
        appendLog("已停止 WebSocket 服务器")
    }

    override fun onDestroy() {
        super.onDestroy()
        JavaWebSocketServerService.stopService(this)  // 页面销毁时停止服务，防止泄漏
    }
}
