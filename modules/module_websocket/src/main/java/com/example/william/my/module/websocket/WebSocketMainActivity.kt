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
class WebSocketMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── OkHttp ──", ""))
        routerItems.add(RouterItem("OkHttp WebSocket", RouterPath.WebSocket.OkHttpWebSocketClient))
        routerItems.add(RouterItem("OkHttp WebSocket (RxJava 封装)", RouterPath.WebSocket.OkHttpWebSocketClientRx))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Java-WebSocket ──", ""))
        routerItems.add(RouterItem("Java-WebSocket（内置本地服务端）", RouterPath.WebSocket.JavaWebSocketClient))
        routerItems.add(RouterItem("Java-WebSocket (RxJava 封装)", RouterPath.WebSocket.JavaWebSocketClientRx))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Netty TCP ──", ""))
        routerItems.add(RouterItem("Netty TCP Socket（内置本地服务端）", RouterPath.WebSocket.NettyWebSocketClient))
        routerItems.add(RouterItem("Netty TCP Socket (RxJava 封装)", RouterPath.WebSocket.NettyWebSocketClientRx))
        return routerItems
    }
}
