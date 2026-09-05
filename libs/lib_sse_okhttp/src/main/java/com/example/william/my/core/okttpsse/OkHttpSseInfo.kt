package com.example.william.my.core.okttpsse

import okhttp3.Response

/**
 * OkHttp SSE 事件密封类
 */
sealed class OkHttpSseInfo {
    /** 连接已建立 */
    data class Open(val response: Response) : OkHttpSseInfo()

    /** 收到 SSE 事件数据 */
    data class Event(
        val id: String?,
        val type: String?,
        val data: String,
    ) : OkHttpSseInfo()

    /** 连接已关闭 */
    data class Closed(val reason: String = "") : OkHttpSseInfo()

    /** 连接发生异常或失败 */
    data class Error(val throwable: Throwable, val response: Response? = null) : OkHttpSseInfo()
}
