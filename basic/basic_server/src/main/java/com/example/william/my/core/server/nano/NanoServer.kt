package com.example.william.my.core.server.nano

import android.app.Application
import com.example.william.my.core.nanohttpd.NanoHttpLogger
import com.example.william.my.core.nanohttpd.NanoHttpServer
import com.example.william.my.core.nanohttpd.ServerConfig
import java.io.IOException

/**
 * NanoHTTPD 轻量级 HTTP 服务器（支持静态文件、登录与上传下载接口）
 */
class NanoServer(context: Application) : NanoHttpServer(context = context, config = ServerConfig(port = DEFAULT_SERVER_PORT)) {

    override fun serve(session: IHTTPSession): Response = parseRequest(session)

    private fun parseRequest(session: IHTTPSession): Response = when (session.method) {
        Method.GET -> parseGetRequest(session)
        Method.POST -> parsePostRequest(session)
        else -> responseNotFound()
    }

    private fun parseGetRequest(session: IHTTPSession): Response {
        val uri = session.uri
        var filename = uri.substring(1)
        if (uri == "/") {
            filename = "index.html"
        }
        if (uri == "/login") {
            return newFixedLengthResponse("登录成功")
        }
        val (mimeType, isAscii) = getMimeType(filename)
        return if (isAscii) {
            loadHtml(filename, mimeType)
        } else {
            loadBinary(filename, mimeType)
        }
    }

    private fun parsePostRequest(session: IHTTPSession): Response = session.parameters?.let { params ->
        when (session.uri) {
            "/login" -> {
                newFixedLengthResponse("登录成功")
            }

            "/upload" -> {
                val body: Map<String, String> = HashMap()
                try {
                    session.parseBody(body)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: ResponseException) {
                    e.printStackTrace()
                }
                val head = session.headers
                for ((key, value) in head) {
                    NanoHttpLogger.debug("$key $value")
                }
                for ((key, value) in params) {
                    NanoHttpLogger.debug("$key $value")
                }
                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_HTML,
                    "{\"message\":\"上传成功\",\"status\":0}",
                )
            }

            "/download" -> newFixedLengthResponse(
                Response.Status.OK,
                MIME_HTML,
                "{\"message\":\"开始下载\",\"status\":0}",
            )

            else -> responseNotFound()
        }
    } ?: responseBadRequest()

    companion object {
        const val DEFAULT_SERVER_PORT = 5567
    }
}
