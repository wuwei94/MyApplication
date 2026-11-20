package com.example.william.my.core.server.service

import android.content.Context
import com.example.william.my.basic.basic_shared.router.service.server.JavaWebSocketServerService
import javax.inject.Inject
import com.example.william.my.core.server.javaws.JavaWebSocketServerService as JavaWsService

/**
 * Java-WebSocket 服务端服务实现
 */
class JavaWebSocketServerServiceImpl @Inject constructor() : JavaWebSocketServerService {

    override fun startServer(context: Context) {
        JavaWsService.startService(context)
    }

    override fun stopServer(context: Context) {
        JavaWsService.stopService(context)
    }
}
