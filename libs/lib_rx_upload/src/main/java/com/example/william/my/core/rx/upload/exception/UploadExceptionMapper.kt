package com.example.william.my.core.rx.upload.exception

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.exception.ExceptionHandler

/** 将上传传输异常转换为项目统一异常。 */
internal fun Throwable.toUploadApiException(): ApiException = when (this) {
    is ApiException -> this
    is UploadHttpException -> ApiException(this, statusCode).apply {
        message = ExceptionHandler.extractErrorMessage(responseBody)
            ?: responseBody.takeIf { it.isNotBlank() }
            ?: this@toUploadApiException.message?.takeIf { it.isNotBlank() }
            ?: ApiException.DEFAULT_MESSAGE
    }

    else -> ExceptionHandler.handleException(this)
}
