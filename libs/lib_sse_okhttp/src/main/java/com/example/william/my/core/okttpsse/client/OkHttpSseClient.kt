package com.example.william.my.core.okttpsse.client

import android.os.Handler
import android.os.Looper
import com.example.william.my.core.okttpsse.OkHttpSseLogger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * OkHttp SSE 客户端（普通回调版本）
 *
 * 基于 okhttp-sse 的 EventSourceListener 封装，提供直观的接口监听与主线程调度。
 */
object OkHttpSseClient {

    private val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // SSE 必须设为 0（长连接不超时）
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val eventSourceMap = ConcurrentHashMap<String, EventSource>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun connect(
        url: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
        listener: OkHttpSseListener? = null
    ): EventSource {
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Accept", "text/event-stream")

        if (jsonBody != null) {
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
        }

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        return connect(url, requestBuilder.build(), defaultClient, listener)
    }

    fun connect(
        url: String,
        request: Request,
        okHttpClient: OkHttpClient = defaultClient,
        listener: OkHttpSseListener? = null
    ): EventSource {
        eventSourceMap[url]?.let { existing ->
            return existing
        }

        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                eventSourceMap[url] = eventSource
                OkHttpSseLogger.debug("SSE onOpen: $url")
                mainHandler.post { listener?.onOpen(eventSource, response) }
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                OkHttpSseLogger.debug("SSE onEvent: type=$type, data=$data")
                mainHandler.post { listener?.onEvent(eventSource, id, type, data) }
            }

            override fun onClosed(eventSource: EventSource) {
                eventSourceMap.remove(url)
                OkHttpSseLogger.debug("SSE onClosed: $url")
                mainHandler.post { listener?.onClosed(eventSource) }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                eventSourceMap.remove(url)
                OkHttpSseLogger.error("SSE onFailure: ${t?.message}", t)
                mainHandler.post { listener?.onFailure(eventSource, t, response) }
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(request, eventSourceListener)
        eventSourceMap[url] = eventSource
        return eventSource
    }

    fun cancel(url: String) {
        eventSourceMap[url]?.let { eventSource ->
            try {
                eventSource.cancel()
            } catch (e: Exception) {
                OkHttpSseLogger.error("cancel failed: $url", e)
            }
        }
        eventSourceMap.remove(url)
    }

    fun cancelAll() {
        eventSourceMap.values.forEach { eventSource ->
            try {
                eventSource.cancel()
            } catch (e: Exception) {
                OkHttpSseLogger.error("cancelAll failed", e)
            }
        }
        eventSourceMap.clear()
    }

    fun isConnected(url: String): Boolean {
        return eventSourceMap.containsKey(url)
    }
}
