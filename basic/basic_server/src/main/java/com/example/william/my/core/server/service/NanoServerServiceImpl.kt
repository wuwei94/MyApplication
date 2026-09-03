package com.example.william.my.core.server.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.server.NanoServerService
import com.example.william.my.core.server.nano.NanoServerService as NanoHttpService

/**
 * NanoHTTPD 服务端路由服务实现
 */
@Route(path = RouterPath.Server.Nano)
class NanoServerServiceImpl : NanoServerService {

    override fun startServer(context: Context) {
        NanoHttpService.startService(context)
    }

    override fun stopServer(context: Context) {
        NanoHttpService.stopService(context)
    }

    override fun init(context: Context) {}
}
