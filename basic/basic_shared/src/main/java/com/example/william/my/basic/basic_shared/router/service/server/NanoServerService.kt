package com.example.william.my.basic.basic_shared.router.service.server

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * NanoHTTPD 服务端路由服务接口
 */
interface NanoServerService : IProvider {
    fun startServer(context: Context)
    fun stopServer(context: Context)
}
