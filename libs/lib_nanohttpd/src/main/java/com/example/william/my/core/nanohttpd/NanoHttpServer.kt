package com.example.william.my.core.nanohttpd

import android.content.Context
import android.content.res.AssetManager
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

open class NanoHttpServer(
    private val context: Context? = null,
    private val config: ServerConfig = ServerConfig(),
    private val lifecycle: ServerLifecycle? = null,
) : NanoHTTPD(config.port) {

    fun startServer() {
        try {
            start(config.timeout, false)
            lifecycle?.onServerStarted(config.port)
        } catch (e: IOException) {
            lifecycle?.onServerError(e)
        }
    }

    fun stopServer() {
        stop()
        lifecycle?.onServerStopped()
    }

    fun isServerRunning(): Boolean = isAlive

    // region 静态文件服务

    fun loadHtml(filename: String, mimeType: String): Response {
        val assetManager = context?.assets ?: return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR, MIME_HTML, "Context not available"
        )
        val response = StringBuilder()
        try {
            val isr = assetManager.open(filename, AssetManager.ACCESS_BUFFER)
            val reader = BufferedReader(InputStreamReader(isr))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return newFixedLengthResponse(Response.Status.NOT_FOUND, mimeType, "")
        }
        return newFixedLengthResponse(Response.Status.OK, mimeType, response.toString())
    }

    fun loadBinary(filename: String, mimeType: String): Response {
        val assetManager = context?.assets ?: return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR, MIME_HTML, "Context not available"
        )
        return try {
            val isr = assetManager.open(filename)
            newFixedLengthResponse(Response.Status.OK, mimeType, isr, isr.available().toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.NOT_FOUND, mimeType, "")
        }
    }

    // endregion

    // region 标准 HTTP 响应

    fun responseNotFound(): Response {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "NOT_FOUND")
    }

    fun responseBadRequest(message: String = "请求参数错误"): Response {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_HTML, message)
    }

    // endregion

    // region MIME 类型检测

    fun getMimeType(filename: String): Pair<String, Boolean> {
        return when {
            filename.contains(".html") || filename.contains(".htm") -> "text/html" to true
            filename.contains(".js") -> "text/javascript" to true
            filename.contains(".css") -> "text/css" to true
            filename.contains(".gif") -> "image/gif" to false
            filename.contains(".jpeg") || filename.contains(".jpg") -> "image/jpeg" to false
            filename.contains(".png") -> "image/png" to false
            filename.contains(".svg") -> "image/svg+xml" to false
            else -> "text/html" to true
        }
    }

    // endregion
}
