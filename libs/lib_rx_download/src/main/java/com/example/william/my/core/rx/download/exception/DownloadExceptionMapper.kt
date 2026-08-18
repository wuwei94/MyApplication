package com.example.william.my.core.rx.download.exception

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.exception.ExceptionHandler

/** 将下载传输异常转换为项目统一异常。 */
internal fun Throwable.toDownloadApiException(): ApiException {
    return when (this) {
        is ApiException -> this
        is DownloadHttpException -> ApiException(this, statusCode).apply {
            message = ExceptionHandler.extractErrorMessage(responseBody)
                ?: responseBody.takeIf { it.isNotBlank() }
                ?: this@toDownloadApiException.message?.takeIf { it.isNotBlank() }
                ?: ApiException.DEFAULT_MESSAGE
        }

        else -> ExceptionHandler.handleException(this)
    }
}
