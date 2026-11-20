package com.example.william.my.core.server.service

import android.content.Context
import com.example.william.my.basic.basic_shared.router.service.server.NanoServerService
import javax.inject.Inject
import com.example.william.my.core.server.nano.NanoServerService as NanoHttpService

/**
 * NanoHTTPD 服务端服务实现
 */
class NanoServerServiceImpl @Inject constructor() : NanoServerService {

    override fun startServer(context: Context) {
        NanoHttpService.startService(context)
    }

    override fun stopServer(context: Context) {
        NanoHttpService.stopService(context)
    }
}
