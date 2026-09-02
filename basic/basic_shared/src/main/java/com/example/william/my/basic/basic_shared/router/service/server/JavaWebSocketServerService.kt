package com.example.william.my.basic.basic_shared.router.service.server

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * Java-WebSocket 服务端路由服务接口
 */
interface JavaWebSocketServerService : IProvider {
    fun startServer(context: Context)
    fun stopServer(context: Context)
}