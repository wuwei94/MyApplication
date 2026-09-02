package com.example.william.my.core.server.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.server.JavaWebSocketServerService
import com.example.william.my.core.server.javaws.JavaWebSocketServerService as JavaWsService

/**
 * Java-WebSocket 服务端路由服务实现
 */
@Route(path = RouterPath.Server.JavaWebSocket)
class JavaWebSocketServerServiceImpl : JavaWebSocketServerService {

    override fun startServer(context: Context) {
        JavaWsService.startService(context)
    }

    override fun stopServer(context: Context) {
        JavaWsService.stopService(context)
    }

    override fun init(context: Context) {}
}