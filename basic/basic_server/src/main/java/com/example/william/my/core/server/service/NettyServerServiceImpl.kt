package com.example.william.my.core.server.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.server.NettyServerService
import com.example.william.my.core.server.netty.NettyWebSocketServerService

/**
 * Netty TCP 服务端路由服务实现
 */
@Route(path = RouterPath.Server.Netty)
class NettyServerServiceImpl : NettyServerService {

    override fun startServer(context: Context) {
        NettyWebSocketServerService.startService(context)
    }

    override fun stopServer(context: Context) {
        NettyWebSocketServerService.stopService(context)
    }

    override fun init(context: Context) {}
}
