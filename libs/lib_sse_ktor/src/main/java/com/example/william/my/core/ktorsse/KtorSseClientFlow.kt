package com.example.william.my.core.ktorsse

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Ktor SSE 客户端（Kotlin Coroutines Flow 封装）
 *
 * 基于 Ktor Client + SSE Plugin 实现，使用 Kotlin Coroutines Flow 收集 SSE 事件。
 */
object KtorSseClientFlow {

    private val defaultClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(SSE)
        }
    }

    fun createEventSource(
        urlString: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
        httpClient: HttpClient = defaultClient
    ): Flow<KtorSseInfo> = flow {
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
                emit(KtorSseInfo.Open)
                incoming.collect { sseEvent ->
                    KtorSseLogger.debug("Ktor SSE event: ${sseEvent.data}")
                    emit(KtorSseInfo.Event(sseEvent.id, sseEvent.event, sseEvent.data))
                }
            }
            KtorSseLogger.debug("Ktor SSE closed: $urlString")
            emit(KtorSseInfo.Closed("Server closed"))
        } catch (e: Throwable) {
            KtorSseLogger.error("Ktor SSE error: ${e.message}", e)
            emit(KtorSseInfo.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}
