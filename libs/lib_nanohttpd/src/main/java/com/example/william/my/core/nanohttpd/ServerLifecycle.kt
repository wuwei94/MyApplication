package com.example.william.my.core.nanohttpd

interface ServerLifecycle {
    fun onServerStarted(port: Int)
    fun onServerStopped()
    fun onServerError(e: Exception)
}
