package com.example.william.my.core.server.javaws

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils

class JavaWebSocketServerService : Service() {

    private var server: JavaWebSocketServer? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        server = JavaWebSocketServer()
        server?.setOnMessageListener(object : JavaWebSocketServer.OnMessageListener {
            override fun onStart() {
                logcat("WebSocket Server started on port ${JavaWebSocketServer.DEFAULT_PORT}")
            }

            override fun onClientConnected(remoteAddress: String) {
                logcat("Client connected: $remoteAddress")
            }

            override fun onMessage(remoteAddress: String, message: String) {
                logcat("onMessage: $remoteAddress - $message")
            }

            override fun onClientDisconnected(remoteAddress: String) {
                logcat("Client disconnected: $remoteAddress")
            }

            override fun onError(throwable: Exception) {
                logcat("onError: ${throwable.message}")
            }
        })
        try {
            server?.start()
            logcat("Start JavaWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Start JavaWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server?.stop()
            logcat("Stop JavaWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Stop JavaWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

    private fun logcat(msg: String) {
        Utils.logcat(TAG, msg)
    }

    companion object {

        private val TAG = JavaWebSocketServerService::class.java.simpleName

        @JvmStatic
        fun startService(context: Context) {
            val intent = Intent(context, JavaWebSocketServerService::class.java)
            context.startService(intent)
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, JavaWebSocketServerService::class.java)
            context.stopService(intent)
        }
    }
}
