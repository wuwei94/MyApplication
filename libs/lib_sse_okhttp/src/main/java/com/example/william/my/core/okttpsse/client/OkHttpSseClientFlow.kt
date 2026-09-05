package com.example.william.my.core.okttpsse.client

import com.example.william.my.core.okttpsse.OkHttpSseInfo
import com.example.william.my.core.okttpsse.OkHttpSseLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
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
 * OkHttp SSE 客户端（Kotlin Coroutines Flow 封装）
 *
 * 使用 callbackFlow 将 SSE 数据流转为响应式 Flow。
 * 协程生命周期结束或主动取消时，awaitClose 自动断开底层 EventSource。
 */
object OkHttpSseClientFlow {

    private val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // SSE 必须设为 0（长连接不超时）
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val eventSourceMap = ConcurrentHashMap<String, EventSource>()

    fun createEventSource(
        url: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): Flow<OkHttpSseInfo> {
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

        return createEventSource(url, requestBuilder.build(), defaultClient)
    }

    fun createEventSource(
        url: String,
        request: Request,
        okHttpClient: OkHttpClient = defaultClient,
    ): Flow<OkHttpSseInfo> = callbackFlow {
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                eventSourceMap[url] = eventSource
                OkHttpSseLogger.debug("SSE Flow onOpen: $url")
                trySend(OkHttpSseInfo.Open(response))
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                OkHttpSseLogger.debug("SSE Flow onEvent: $data")
                trySend(OkHttpSseInfo.Event(id, type, data))
            }

            override fun onClosed(eventSource: EventSource) {
                eventSourceMap.remove(url)
                OkHttpSseLogger.debug("SSE Flow onClosed: $url")
                trySend(OkHttpSseInfo.Closed("Server closed"))
                close()
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?,
            ) {
                eventSourceMap.remove(url)
                val error = t ?: Exception("SSE Error with status: ${response?.code}")
                OkHttpSseLogger.error("SSE Flow onFailure: ${error.message}", error)
                trySend(OkHttpSseInfo.Error(error, response))
                close()
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(request, listener)
        eventSourceMap[url] = eventSource

        awaitClose {
            OkHttpSseLogger.debug("SSE Flow awaitClose: $url")
            try {
                eventSource.cancel()
            } catch (e: Exception) {
                OkHttpSseLogger.error("cancel failed in awaitClose: $url", e)
            }
            eventSourceMap.remove(url)
        }
    }.flowOn(Dispatchers.IO)

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

    fun isConnected(url: String): Boolean = eventSourceMap.containsKey(url)
}
