package com.example.william.my.core.okttpsse.client

import com.example.william.my.core.okttpsse.OkHttpSseInfo
import com.example.william.my.core.okttpsse.OkHttpSseLogger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
 * OkHttp SSE 客户端（RxJava 封装）
 *
 * 将 SSE 事件转换为 Observable<OkHttpSseInfo> 数据流。
 */
object OkHttpSseClientRx {

    private val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val eventSourceMap = ConcurrentHashMap<String, EventSource>()
    private val disposableMap = ConcurrentHashMap<String, Disposable>()

    fun createEventSource(
        url: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): Observable<OkHttpSseInfo> {
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
    ): Observable<OkHttpSseInfo> = Observable.create { emitter: ObservableEmitter<OkHttpSseInfo> ->
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                eventSourceMap[url] = eventSource
                OkHttpSseLogger.debug("SSE Rx onOpen: $url")
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpSseInfo.Open(response))
                }
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                OkHttpSseLogger.debug("SSE Rx onEvent: $data")
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpSseInfo.Event(id, type, data))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                eventSourceMap.remove(url)
                disposableMap.remove(url)
                OkHttpSseLogger.debug("SSE Rx onClosed: $url")
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpSseInfo.Closed("Server closed"))
                    emitter.onComplete()
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?,
            ) {
                eventSourceMap.remove(url)
                disposableMap.remove(url)
                val error = t ?: Exception("SSE Error with status: ${response?.code}")
                OkHttpSseLogger.error("SSE Rx onFailure: ${error.message}", error)
                if (!emitter.isDisposed) {
                    emitter.onNext(OkHttpSseInfo.Error(error, response))
                    emitter.onComplete()
                }
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(request, listener)
        eventSourceMap[url] = eventSource

        emitter.setCancellable {
            try {
                eventSource.cancel()
            } catch (e: Exception) {
                OkHttpSseLogger.error("cancel in emitter failed: $url", e)
            }
            eventSourceMap.remove(url)
            disposableMap.remove(url)
        }
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun cancel(url: String) {
        eventSourceMap[url]?.let { eventSource ->
            try {
                eventSource.cancel()
            } catch (e: Exception) {
                OkHttpSseLogger.error("cancel failed: $url", e)
            }
        }
        eventSourceMap.remove(url)
        disposableMap[url]?.dispose()
        disposableMap.remove(url)
    }

    fun subscribe(
        url: String,
        jsonBody: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): Disposable {
        disposableMap[url]?.dispose()
        val disposable = createEventSource(url = url, jsonBody = jsonBody, headers = headers).subscribe()
        disposableMap[url] = disposable
        return disposable
    }
}
