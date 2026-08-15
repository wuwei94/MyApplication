package com.example.william.my.core.rx.download.exception

import java.io.IOException

/** 下载请求收到非预期 HTTP 响应。 */
class DownloadHttpException(
    val statusCode: Int,
    val responseBody: String,
    responseMessage: String,
) : IOException("下载失败：HTTP $statusCode $responseMessage")
