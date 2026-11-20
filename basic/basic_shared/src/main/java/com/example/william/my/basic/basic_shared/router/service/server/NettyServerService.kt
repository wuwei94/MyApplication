package com.example.william.my.basic.basic_shared.router.service.server

import android.content.Context

/**
 * Netty TCP 服务端服务接口
 */
interface NettyServerService {
    fun startServer(context: Context)
    fun stopServer(context: Context)
}
