package com.example.william.my.core.server.service

import android.content.Context
import com.example.william.my.basic.basic_shared.router.service.server.NettyServerService
import com.example.william.my.core.server.netty.NettyWebSocketServerService
import javax.inject.Inject

/**
 * Netty TCP 服务端服务实现
 */
class NettyServerServiceImpl @Inject constructor() : NettyServerService {

    override fun startServer(context: Context) {
        NettyWebSocketServerService.startService(context)
    }

    override fun stopServer(context: Context) {
        NettyWebSocketServerService.stopService(context)
    }
}
