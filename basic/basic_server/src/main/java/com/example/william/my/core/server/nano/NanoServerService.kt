package com.example.william.my.core.server.nano

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils
import java.io.IOException

/**
 * NanoHTTPD 服务器 Service（随服务启动/停止 HTTP 服务器）
 */
class NanoServerService : Service() {

    private var nanoServer: NanoServer? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        nanoServer = NanoServer(application)
        Thread {
            try {
                nanoServer?.start(3000)
                logcat("Start NanoServerService Success...")
            } catch (e: IOException) {
                logcat("Start NanoServerService Failed: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            nanoServer?.stop()
            logcat("Stop NanoServerService Success...")
        } catch (e: Exception) {
            logcat("Stop NanoServerService Failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

    private fun logcat(msg: String) {
        Utils.logcat(TAG, msg)
    }

    companion object {

        private val TAG = NanoServerService::class.java.simpleName

        @JvmStatic
        fun startService(context: Context) {
            val intent = Intent(context, NanoServerService::class.java)
            context.startService(intent)
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, NanoServerService::class.java)
            context.stopService(intent)
        }
    }
}
