package com.example.william.my.basic.basic_shared.router.service.server

import android.content.Context

/**
 * Java-WebSocket 服务端服务接口
 */
interface JavaWebSocketServerService {
    fun startServer(context: Context)
    fun stopServer(context: Context)
}
