package com.example.william.my.core.ktor.exception

/**
 * 统一网络异常
 *
 * 只保留错误码、消息与原始原因，供调用方按与其他网络库一致的契约处理失败。
 */
class ApiException(throwable: Throwable, var code: Int) : Exception(throwable) {

    override var message: String = ""

    /**
     * 本地异常错误码
     */
    object Error {
        const val UNKNOWN = 1000
        const val CONNECT_ERROR = 1001
        const val TIMEOUT_ERROR = 1002
        const val SSL_ERROR = 1003
        const val PARSE_ERROR = 1004
    }
}
