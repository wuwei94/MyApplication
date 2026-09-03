package com.example.william.my.core.server.netty

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils

class NettyWebSocketServerService : Service() {

    private var server: NettyWebSocketServer? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        server = NettyWebSocketServer()
        server?.setOnMessageListener(object : NettyWebSocketServer.OnMessageListener {
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
        })
        Thread {
            try {
                server?.start()
                logcat("Start NettyWebSocketServerService Success...")
            } catch (e: Exception) {
                logcat("Start NettyWebSocketServerService Failed: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server?.stop()
            logcat("Stop NettyWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Stop NettyWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

    private fun logcat(msg: String) {
        Utils.logcat(TAG, msg)
    }

    companion object {

        private val TAG = NettyWebSocketServerService::class.java.simpleName

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
