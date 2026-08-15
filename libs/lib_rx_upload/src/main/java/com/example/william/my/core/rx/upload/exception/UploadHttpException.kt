package com.example.william.my.core.rx.upload.exception

import java.io.IOException

/** 上传请求收到非 2xx HTTP 响应。 */
class UploadHttpException(
    val statusCode: Int,
    val responseBody: String,
    responseMessage: String,
) : IOException("上传失败：HTTP $statusCode $responseMessage")
