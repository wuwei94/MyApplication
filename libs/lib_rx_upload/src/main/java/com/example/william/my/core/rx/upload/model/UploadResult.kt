package com.example.william.my.core.rx.upload.model

/** 上传成功结果，保留原始 HTTP 响应信息。 */
data class UploadResult(
    val statusCode: Int,
    val message: String,
    val headers: Map<String, List<String>>,
    val body: String,
)
