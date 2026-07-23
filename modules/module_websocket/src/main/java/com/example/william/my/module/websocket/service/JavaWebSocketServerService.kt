package com.example.william.my.module.websocket.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.javaws.server.JavaWebSocketServer
import com.example.william.my.core.javaws.server.JavaWebSocketServerListener
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

/**
 * Java-WebSocket 服务端 Service
 *
 * 通过 JavaWebSocketServer 启动本地 WebSocket 服务器（默认端口 5566）。
 * 收到消息后自动 Echo 回传，用于演示 WebSocket 服务端的基本用法。
 */
class JavaWebSocketServerService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        try {
            JavaWebSocketServer.start(DEFAULT_PORT, object : JavaWebSocketServerListener() {
                override fun onStart() {
                    logcat("WebSocket Server started on port $DEFAULT_PORT")
                }

                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    logcat("onOpen: ${conn.remoteSocketAddress}")
                }

                override fun onMessage(conn: WebSocket, message: String) {
                    logcat("onMessage: $message")
                    conn.send("Echo: $message")
                }

                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    logcat("onClose: code=$code reason=$reason remote=$remote")
                }

                override fun onError(conn: WebSocket?, ex: Exception) {
                    logcat("onError: ${ex.message}")
                }
            })
            logcat("Start JavaWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Start JavaWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            JavaWebSocketServer.stop()
            logcat("Stop JavaWebSocketServerService Success...")
        } catch (e: Exception) {
            logcat("Stop JavaWebSocketServerService Failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun logcat(msg: String) {
        Utils.logcat(TAG, msg)
    }

    companion object {

        private val TAG = JavaWebSocketServerService::class.java.simpleName
        private const val DEFAULT_PORT = 5566

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
