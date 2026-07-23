package com.example.william.my.module.websocket

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * WebSocket 示例入口
 *
 * 通过下方列表选择不同的 WebSocket 实现方式：
 * - OkHttp WebSocket 普通版本 / RxJava 封装
 * - Java-WebSocket 普通版本 / RxJava 封装
 * - Netty TCP 普通版本 / RxJava 封装
 *
 * 注意：每个客户端页面都有"启动服务端"按钮，可直接启动本地服务器
 */
@Route(path = RouterPath.WebSocket.Main)
class WebSocketActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("OkHttpWebSocketClient", RouterPath.WebSocket.OkHttpWebSocket.OkHttpWebSocketClient),
            RouterItem("OkHttpWebSocketClientRx", RouterPath.WebSocket.OkHttpWebSocket.OkHttpWebSocketClientRx),
            RouterItem("JavaWebSocketClient", RouterPath.WebSocket.JavaWebSocket.JavaWebSocketClient),
            RouterItem("JavaWebSocketClientRx", RouterPath.WebSocket.JavaWebSocket.JavaWebSocketClientRx),
            RouterItem("NettyWebSocketClient", RouterPath.WebSocket.NettyWebSocket.NettyWebSocketClient),
            RouterItem("NettyWebSocketClientRx", RouterPath.WebSocket.NettyWebSocket.NettyWebSocketClientRx),
        )
    }
}
