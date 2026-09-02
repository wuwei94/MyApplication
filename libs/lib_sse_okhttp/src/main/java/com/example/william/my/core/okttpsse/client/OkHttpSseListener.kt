package com.example.william.my.core.okttpsse.client

import okhttp3.Response
import okhttp3.sse.EventSource

/**
 * OkHttp SSE 事件监听器
 */
abstract class OkHttpSseListener {
    /** 连接已建立 */
    open fun onOpen(eventSource: EventSource, response: Response) {}

    /** 收到 SSE 事件 */
    open fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {}

    /** 连接已关闭 */
    open fun onClosed(eventSource: EventSource) {}

    /** 连接失败或发生异常 */
    open fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {}
}
