package com.example.william.my.core.server

import android.content.Context
import com.example.william.my.core.server.javaws.JavaWebSocketServerService
import com.example.william.my.core.server.nano.NanoServerService
import com.example.william.my.core.server.netty.NettyWebSocketServerService

object ServerManager {

    fun startNanoServer(context: Context) {
        NanoServerService.startService(context)
    }

    fun stopNanoServer(context: Context) {
        NanoServerService.stopService(context)
    }

    fun startJavaWebSocketServer(context: Context) {
        JavaWebSocketServerService.startService(context)
    }

    fun stopJavaWebSocketServer(context: Context) {
        JavaWebSocketServerService.stopService(context)
    }

    fun startNettyServer(context: Context) {
        NettyWebSocketServerService.startService(context)
    }

    fun stopNettyServer(context: Context) {
        NettyWebSocketServerService.stopService(context)
    }

    fun startAll(context: Context) {
        startNanoServer(context)
        startJavaWebSocketServer(context)
        startNettyServer(context)
    }

    fun stopAll(context: Context) {
        stopNanoServer(context)
        stopJavaWebSocketServer(context)
        stopNettyServer(context)
    }
}
