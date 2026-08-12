package com.example.william.my.core.okhttp.media

import okhttp3.MediaType.Companion.toMediaType

/**
 * HTTP 媒体类型常量
 */
object MediaTypes {
    val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    val MEDIA_TYPE_MULTIPART = "multipart/form-data".toMediaType()
}
