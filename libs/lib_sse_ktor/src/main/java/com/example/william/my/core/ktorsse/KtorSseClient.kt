package com.example.william.my.core.ktorsse

import android.os.Handler
import android.os.Looper
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Ktor SSE 客户端（普通回调版本）
 *
 * 基于 Ktor Client + SSE Plugin 实现，通过内部后台协程收集流，并在主线程调度 KtorSseListener 回调。
 */
object KtorSseClient {

    private val defaultClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(SSE)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobMap = ConcurrentHashMap<String, Job>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun connect(
        urlString: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
        httpClient: HttpClient = defaultClient,
        listener: KtorSseListener? = null
    ): Job {
        cancel(urlString)

        val job = scope.launch {
            KtorSseLogger.debug("Ktor SSE connecting: $urlString")
            try {
                httpClient.sse(
                    urlString = urlString,
                    request = {
                        if (jsonBody != null) {
                            method = HttpMethod.Post
                            setBody(jsonBody)
                            this.headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                        this.headers.append(HttpHeaders.Accept, "text/event-stream")
                        headers.forEach { (k, v) ->
                            this.headers.append(k, v)
                        }
                    }
                ) {
                    mainHandler.post { listener?.onOpen() }
                    incoming.collect { sseEvent ->
                        KtorSseLogger.debug("Ktor SSE event: ${sseEvent.data}")
                        mainHandler.post {
                            listener?.onEvent(sseEvent.id, sseEvent.event, sseEvent.data)
                        }
                    }
                }
                jobMap.remove(urlString)
                KtorSseLogger.debug("Ktor SSE closed: $urlString")
                mainHandler.post { listener?.onClosed("Server closed") }
            } catch (e: Throwable) {
                jobMap.remove(urlString)
                KtorSseLogger.error("Ktor SSE error: ${e.message}", e)
                mainHandler.post { listener?.onFailure(e) }
            }
        }

        jobMap[urlString] = job
        return job
    }

    fun cancel(urlString: String) {
        jobMap[urlString]?.cancel()
        jobMap.remove(urlString)
    }

    fun cancelAll() {
        jobMap.values.forEach { it.cancel() }
        jobMap.clear()
    }

    fun isConnected(urlString: String): Boolean {
        return jobMap[urlString]?.isActive == true
    }
}
