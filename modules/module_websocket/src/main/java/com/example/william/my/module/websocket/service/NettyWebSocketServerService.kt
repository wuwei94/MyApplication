package com.example.william.my.module.websocket.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.netty.server.NettyServerHandler

/**
 * Netty TCP 服务端 Service
 *
 * 通过 NettyServer 启动本地 TCP 服务器（默认端口 5566）。
 * 收到消息后自动 Echo 回传，用于演示 TCP 服务端的基本用法。
 */
class NettyWebSocketServerService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Thread {
            try {
                NettyServer.start(
                    port = DEFAULT_PORT,
                    listener = object : NettyServerHandler.OnMessageListener {
                        override fun onClientConnected(remoteAddress: String) {
                            logcat("Client connected: $remoteAddress")
                        }

                        override fun onClientDisconnected(remoteAddress: String) {
                            logcat("Client disconnected: $remoteAddress")
                        }

                        override fun onMessage(remoteAddress: String, message: String) {
                            logcat("onMessage: $remoteAddress - $message")
                        }

                        override fun onError(remoteAddress: String, throwable: Throwable) {
                            logcat("onError: $remoteAddress - ${throwable.message}")
                        }
                    }
                )
                logcat("Start NettyWebSocketServerService Success...")
            } catch (e: Exception) {
                logcat("Start NettyWebSocketServerService Failed: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            NettyServer.stop()
            logcat("Stop NettyWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Stop NettyWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun logcat(msg: String) {
        Utils.logcat(TAG, msg)
    }

    companion object {

        private val TAG = NettyWebSocketServerService::class.java.simpleName
        private const val DEFAULT_PORT = 5567

        @JvmStatic
        fun startService(context: Context) {
            val intent = Intent(context, NettyWebSocketServerService::class.java)
            context.startService(intent)
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, NettyWebSocketServerService::class.java)
            context.stopService(intent)
        }
    }
}
