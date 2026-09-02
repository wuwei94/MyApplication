package com.example.william.my.module.socket

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Socket 长连接示例入口（WebSocket & TCP Socket）
 *
 * 通过下方列表选择不同的 Socket 通信实现：
 * - WebSocket（应用层）：OkHttp WebSocket / Java-WebSocket（普通 / RxJava / Flow）
 * - TCP Socket（传输层）：Netty TCP Socket（普通 / RxJava / Flow）
 */
@Route(path = RouterPath.Socket.Main)
class SocketMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── OkHttp WebSocket ──", ""))
        routerItems.add(RouterItem("OkHttp WebSocket", RouterPath.Socket.OkHttpWebSocketClient))
        routerItems.add(RouterItem("OkHttp WebSocket (RxJava 封装)", RouterPath.Socket.OkHttpWebSocketClientRx))
        routerItems.add(RouterItem("OkHttp WebSocket (Flow 封装)", RouterPath.Socket.OkHttpWebSocketClientFlow))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Java-WebSocket ──", ""))
        routerItems.add(RouterItem("Java-WebSocket（内置本地服务端）", RouterPath.Socket.JavaWebSocketClient))
        routerItems.add(RouterItem("Java-WebSocket (RxJava 封装)", RouterPath.Socket.JavaWebSocketClientRx))
        routerItems.add(RouterItem("Java-WebSocket (Flow 封装)", RouterPath.Socket.JavaWebSocketClientFlow))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Netty TCP Socket ──", ""))
        routerItems.add(RouterItem("Netty TCP Socket（内置本地服务端）", RouterPath.Socket.NettyTcpSocketClient))
        routerItems.add(RouterItem("Netty TCP Socket (RxJava 封装)", RouterPath.Socket.NettyTcpSocketClientRx))
        routerItems.add(RouterItem("Netty TCP Socket (Flow 封装)", RouterPath.Socket.NettyTcpSocketClientFlow))
        return routerItems
    }
}

