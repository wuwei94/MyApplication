package com.example.william.my.core.nanohttpd

/**
 * 服务器生命周期回调接口
 */
interface ServerLifecycle {
    fun onServerStarted(port: Int)
    fun onServerStopped()
    fun onServerError(e: Exception)
}
